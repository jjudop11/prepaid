package com.prepaid.payment.service;

import com.prepaid.domain.User;
import com.prepaid.ledger.service.LedgerService;
import com.prepaid.payment.dto.PaymentConfirmRequest;
import com.prepaid.payment.dto.TossPaymentResponse;
import com.prepaid.payment.validation.PaymentValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.http.MediaType;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final RestClient tossRestClient;
    private final LedgerService ledgerService;
    private final PaymentValidator paymentValidator;

    @Transactional
    public void confirmPayment(User user, PaymentConfirmRequest request) {
        // 1. 금액 검증 (최소/최대/일일 한도)
        Long amount = request.getAmount();
        paymentValidator.validateChargeAmount(user.getId(), amount);
        
        // 2. Call Toss API (Blocking call, efficient on Virtual Threads)
        TossPaymentResponse response = tossRestClient.post()
                .uri("/confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(TossPaymentResponse.class);

        if (response == null || !"DONE".equals(response.getStatus())) {
            throw new RuntimeException("Toss Payment Failed or Not Done");
        }

        log.info("Toss Payment Confirmed: {}, Amount: {}", response.getPaymentKey(), response.getTotalAmount());

        // 3. Record Ledger (Transactional)
        ledgerService.recordCharge(user, response.getTotalAmount(), response.getPaymentKey(), response.getOrderId());
        
        // 4. 일일 한도 업데이트
        paymentValidator.updateDailyLimit(user.getId(), response.getTotalAmount());
    }
}
