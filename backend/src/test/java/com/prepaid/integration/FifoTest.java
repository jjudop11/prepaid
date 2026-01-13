package com.prepaid.integration;

import com.prepaid.domain.User;
import com.prepaid.domain.Wallet;
import com.prepaid.ledger.domain.BucketType;
import com.prepaid.ledger.domain.ChargeLot;
import com.prepaid.ledger.domain.LedgerEntry;
import com.prepaid.ledger.domain.SpendAllocation;
import com.prepaid.ledger.repository.ChargeLotRepository;
import com.prepaid.ledger.repository.LedgerEntryRepository;
import com.prepaid.ledger.repository.SpendAllocationRepository;
import com.prepaid.ledger.service.LedgerService;
import com.prepaid.repository.UserRepository;
import com.prepaid.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("local")
@org.springframework.test.context.TestPropertySource(properties = {
        "TOSS_CLIENT_KEY=test_toss_client_key",
        "TOSS_SECRET_KEY=test_toss_secret_key",
        "NAVER_CLIENT_ID=test_naver_client_id",
        "NAVER_CLIENT_SECRET=test_naver_client_secret"
})
class FifoTest {

    @Autowired
    private LedgerService ledgerService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private ChargeLotRepository chargeLotRepository;

    @Autowired
    private SpendAllocationRepository spendAllocationRepository;

    @Autowired
    private LedgerEntryRepository ledgerEntryRepository;

    @Autowired
    private com.prepaid.ledger.repository.LedgerLineRepository ledgerLineRepository;

    private User testUser;
    private Wallet wallet;

    @BeforeEach
    void setUp() {
        tearDown(); // Ensure clean state
        testUser = User.builder()
                .email("fifo@example.com")
                .provider("NAVER")
                .role("ROLE_USER")
                .build();
        testUser = userRepository.save(testUser);

        wallet = Wallet.builder().user(testUser).build();
        wallet = walletRepository.save(wallet);
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        ledgerLineRepository.deleteAll();
        spendAllocationRepository.deleteAll();
        ledgerEntryRepository.deleteAll();
        chargeLotRepository.deleteAll();
        walletRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("무료 포인트(FREE)가 유료 포인트(PAID)보다 먼저 차감되어야 한다")
    void useBalance_should_consume_free_before_paid() {
        // 준비 (Given)
        // 1. 유료 충전 1000
        createChargeLot(wallet, BucketType.PAID, 1000L);
        // 2. 무료 충전 500
        createChargeLot(wallet, BucketType.FREE, 500L);

        // recordCharge를 우회하므로 지갑 잔액을 수동으로 업데이트
        wallet.addBalance(1000L, BucketType.PAID);
        wallet.addBalance(500L, BucketType.FREE);
        walletRepository.save(wallet);

        // 실행 (When)
        // 700 사용 (500 무료 + 200 유료 차감 되어야 함)
        ledgerService.useBalance(testUser, 700L, "merchant_1");

        // 검증 (Then)
        // 1. 지갑 잔액 확인
        Wallet updatedWallet = walletRepository.findByUserId(testUser.getId()).orElseThrow();
        assertThat(updatedWallet.getBalanceFree()).isEqualTo(0L); // 무료 전체 소진
        assertThat(updatedWallet.getBalancePaid()).isEqualTo(800L); // 1000 - 200
        assertThat(updatedWallet.getBalance()).isEqualTo(800L);

        // 2. 소비 할당(Spend Allocation) 확인
        List<SpendAllocation> allocations = spendAllocationRepository.findAllWithChargeLot();
        assertThat(allocations).hasSize(2);

        // 무료 Lot이 완전히 소진되었는지 확인
        boolean freeConsumed = allocations.stream()
                .anyMatch(a -> a.getChargeLot().getBucketType() == BucketType.FREE && a.getAmountConsumed() == 500L);
        // 유료 Lot이 부분적으로 소진되었는지 확인
        boolean paidConsumed = allocations.stream()
                .anyMatch(a -> a.getChargeLot().getBucketType() == BucketType.PAID && a.getAmountConsumed() == 200L);

        assertThat(freeConsumed).isTrue();
        assertThat(paidConsumed).isTrue();
    }

    @Test
    @DisplayName("같은 타입 내에서는 먼저 생성된 포인트(FIFO)가 먼저 차감되어야 한다")
    void useBalance_should_follow_fifo_within_same_bucket() {
        // 준비 (Given)
        // Lot 1: 유료 1000 (과거)
        ChargeLot lot1 = createChargeLot(wallet, BucketType.PAID, 1000L, LocalDateTime.now().minusDays(2));
        // Lot 2: 유료 1000 (최근)
        ChargeLot lot2 = createChargeLot(wallet, BucketType.PAID, 1000L, LocalDateTime.now().minusDays(1));

        wallet.addBalance(2000L, BucketType.PAID);
        walletRepository.save(wallet);

        // 실행 (When)
        // 1500 사용 (Lot 1에서 1000 + Lot 2에서 500 차감 되어야 함)
        ledgerService.useBalance(testUser, 1500L, "merchant_2");

        // 검증 (Then)
        Wallet updatedWallet = walletRepository.findByUserId(testUser.getId()).orElseThrow();
        assertThat(updatedWallet.getBalancePaid()).isEqualTo(500L);

        List<SpendAllocation> allocations = spendAllocationRepository.findAllWithChargeLot();

        // Lot 1은 완전히 소진되어야 함
        SpendAllocation alloc1 = allocations.stream()
                .filter(a -> a.getChargeLot().getId().equals(lot1.getId()))
                .findFirst().orElseThrow();
        assertThat(alloc1.getAmountConsumed()).isEqualTo(1000L);

        // Lot 2는 부분적으로 소진되어야 함
        SpendAllocation alloc2 = allocations.stream()
                .filter(a -> a.getChargeLot().getId().equals(lot2.getId()))
                .findFirst().orElseThrow();
        assertThat(alloc2.getAmountConsumed()).isEqualTo(500L);
    }

    private ChargeLot createChargeLot(Wallet wallet, BucketType bucketType, Long amount) {
        return createChargeLot(wallet, bucketType, amount, LocalDateTime.now());
    }

    private ChargeLot createChargeLot(Wallet wallet, BucketType bucketType, Long amount, LocalDateTime createdAt) {
        ChargeLot lot = ChargeLot.builder()
                .wallet(wallet)
                .bucketType(bucketType)
                .amountTotal(amount)
                .amountRemaining(amount)
                .originalEntryId(System.nanoTime())
                .build();
        // JPA Auditing이 createdAt을 자동으로 설정하므로, FIFO 테스트 시 저장 순서에 의존하거나
        // 수동 설정이 필요한 경우 주의가 필요함.
        // ChargeLotRepository는 createdAt 기준으로 정렬함.
        // 순차적 저장 -> 순차적 ID 및 시간으로 가정.
        chargeLotRepository.save(lot);
        return lot;
    }
}
