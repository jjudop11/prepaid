package com.prepaid.payment.service;

import com.prepaid.domain.User;
import com.prepaid.ledger.service.LedgerService;
import com.prepaid.payment.dto.PaymentConfirmRequest;
import com.prepaid.payment.dto.TossPaymentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.http.MediaType;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final RestClient tossRestClient;
    private final LedgerService ledgerService;

    public void confirmPayment(User user, PaymentConfirmRequest request) {
        // 1. Call Toss API (Blocking call, efficient on Virtual Threads)
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

        // 2. Record Ledger (Transactional)
        ledgerService.recordCharge(user, response.getTotalAmount(), response.getPaymentKey(), response.getOrderId());
    }
}
