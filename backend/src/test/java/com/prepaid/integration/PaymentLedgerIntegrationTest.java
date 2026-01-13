package com.prepaid.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prepaid.domain.User;
import com.prepaid.domain.Wallet;
import com.prepaid.ledger.domain.LedgerEntry;
import com.prepaid.ledger.repository.*;
import com.prepaid.payment.dto.PaymentConfirmRequest;
import com.prepaid.payment.service.PaymentService;
import com.prepaid.repository.UserRepository;
import com.prepaid.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureMockRestServiceServer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@SpringBootTest
@AutoConfigureMockRestServiceServer
@ActiveProfiles("local")
@Transactional
@org.springframework.test.context.TestPropertySource(properties = {
        "TOSS_CLIENT_KEY=test_toss_client_key",
        "TOSS_SECRET_KEY=test_toss_secret_key",
        "NAVER_CLIENT_ID=test_naver_client_id",
        "NAVER_CLIENT_SECRET=test_naver_client_secret"
})
class PaymentLedgerIntegrationTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private MockRestServiceServer mockServer;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private LedgerEntryRepository ledgerEntryRepository;

    @Autowired
    private LedgerLineRepository ledgerLineRepository;

    @Autowired
    private ChargeLotRepository chargeLotRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .email("test@example.com")
                .provider("NAVER")
                .role("ROLE_USER")
                .build();
        userRepository.save(testUser);
    }

    @Test
    @DisplayName("Toss 결제 승인 후 원장(Ledger) 기록 및 지갑 잔액 증가 검증")
    void confirmPayment_should_record_ledger_and_update_balance() throws Exception {
        // 준비 (Given)
        Long amount = 10000L;
        String paymentKey = "test_payment_key";
        String orderId = "test_order_id";

        PaymentConfirmRequest request = new PaymentConfirmRequest();
        request.setPaymentKey(paymentKey);
        request.setOrderId(orderId);
        request.setAmount(amount);

        // Toss API Mock Response
        String tossResponseJson = """
                    {
                        "paymentKey": "%s",
                        "orderId": "%s",
                        "status": "DONE",
                        "totalAmount": %d
                    }
                """.formatted(paymentKey, orderId, amount);

        mockServer.expect(requestTo("https://api.tosspayments.com/v1/payments/confirm"))
                .andRespond(withSuccess(tossResponseJson, MediaType.APPLICATION_JSON));

        // 실행 (When)
        paymentService.confirmPayment(testUser, request);

        // 검증 (Then)
        // 1. 지갑 잔액 확인
        Wallet wallet = walletRepository.findByUserId(testUser.getId()).orElseThrow();
        assertThat(wallet.getBalancePaid()).isEqualTo(amount);
        assertThat(wallet.getBalance()).isEqualTo(amount);

        // 2. 원장 엔트리 확인
        LedgerEntry entry = ledgerEntryRepository.findAll().get(0);
        assertThat(entry.getIdempotencyKey()).isEqualTo(paymentKey);
        assertThat(entry.getTxType().name()).isEqualTo("CHARGE");

        // 3. 원장 라인 확인 (복식부기)
        assertThat(ledgerLineRepository.count()).isEqualTo(2); // 차변 + 대변

        // 4. Charge Lot 확인
        assertThat(chargeLotRepository.count()).isEqualTo(1);
        assertThat(chargeLotRepository.findAll().get(0).getAmountRemaining()).isEqualTo(amount);
    }
}
