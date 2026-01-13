package com.prepaid.integration;

import com.prepaid.domain.User;
import com.prepaid.domain.Wallet;
import com.prepaid.ledger.domain.BucketType;
import com.prepaid.ledger.domain.ChargeLot;
import com.prepaid.ledger.repository.ChargeLotRepository;
import com.prepaid.ledger.repository.LedgerEntryRepository;
import com.prepaid.ledger.repository.LedgerLineRepository;
import com.prepaid.ledger.repository.SpendAllocationRepository;
import com.prepaid.ledger.service.LedgerService;
import com.prepaid.repository.UserRepository;
import com.prepaid.repository.WalletRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("local")
@TestPropertySource(properties = {
        "TOSS_CLIENT_KEY=test_toss_client_key",
        "TOSS_SECRET_KEY=test_toss_secret_key",
        "NAVER_CLIENT_ID=test_naver_client_id",
        "NAVER_CLIENT_SECRET=test_naver_client_secret"
})
public class ConcurrencyTest {

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
    private LedgerLineRepository ledgerLineRepository;

    private User testUser;
    private Wallet wallet;

    @BeforeEach
    void setUp() {
        // 테스트 전 정리
        tearDown();

        testUser = User.builder()
                .email("concurrency@example.com")
                .provider("NAVER")
                .role("ROLE_USER")
                .build();
        testUser = userRepository.save(testUser);

        wallet = Wallet.builder().user(testUser).build();
        wallet = walletRepository.save(wallet);
    }

    @AfterEach
    void tearDown() {
        ledgerLineRepository.deleteAll();
        spendAllocationRepository.deleteAll();
        ledgerEntryRepository.deleteAll();
        chargeLotRepository.deleteAll();
        walletRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("동시성 테스트: 1000원 잔액에서 3명이 동시에 500원 사용 시도 -> 2명 성공, 1명 실패")
    void concurrent_use_balance_should_prevent_overdraft() throws InterruptedException {
        // 준비 (Given)
        Long initialAmount = 1000L;
        // 1000원 충전
        ChargeLot lot = ChargeLot.builder()
                .wallet(wallet)
                .bucketType(BucketType.PAID)
                .amountTotal(initialAmount)
                .amountRemaining(initialAmount)
                .originalEntryId(1L)
                .build();
        chargeLotRepository.save(lot);
        wallet.addBalance(initialAmount, BucketType.PAID);
        walletRepository.save(wallet);

        int threadCount = 3;
        Long useAmount = 500L;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        // 실행 (When)
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    ledgerService.useBalance(testUser, useAmount, "merchant_" + System.currentTimeMillis());
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    // System.out.println("Failed: " + e.getMessage());
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        // 검증 (Then)
        // 기대 결과: 2명 성공 (2 * 500 = 1000), 1명 실패 (잔액 부족)
        assertThat(successCount.get()).isEqualTo(2);
        assertThat(failCount.get()).isEqualTo(1);

        // 잔액 확인
        Wallet updatedWallet = walletRepository.findByUserId(testUser.getId()).orElseThrow();
        assertThat(updatedWallet.getBalancePaid()).isEqualTo(0L);
        assertThat(updatedWallet.getBalance()).isEqualTo(0L);

        // Lot 확인
        ChargeLot updatedLot = chargeLotRepository.findById(lot.getId()).orElseThrow();
        assertThat(updatedLot.getAmountRemaining()).isEqualTo(0L);
    }
}
