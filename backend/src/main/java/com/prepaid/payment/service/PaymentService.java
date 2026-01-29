package com.prepaid.payment.service;

import com.prepaid.domain.User;
import com.prepaid.ledger.service.LedgerService;
import com.prepaid.payment.domain.Payment;
import com.prepaid.payment.dto.PaymentConfirmRequest;
import com.prepaid.payment.dto.TossPaymentResponse;
import com.prepaid.payment.repository.PaymentRepository;
import com.prepaid.payment.validation.PaymentValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.http.MediaType;

import static com.prepaid.common.logging.LoggingUtils.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final RestClient tossRestClient;
    private final LedgerService ledgerService;
    private final PaymentValidator paymentValidator;
    private final PaymentRepository paymentRepository;

    @Transactional
    public void confirmPayment(User user, PaymentConfirmRequest request) {
        // MDC 컨텍스트 설정
        setUserContext(user.getId());
        setTransactionContext("CHARGE", request.getAmount());
        setOrderContext(request.getOrderId());
        
        try {
            // 1. 금액 검증 (최소/최대/일일 한도)
            Long amount = request.getAmount();
            paymentValidator.validateChargeAmount(user.getId(), amount);
            
            // 2. Payment 생성 (PENDING 상태)
            Payment payment = Payment.builder()
                    .orderId(request.getOrderId())
                    .user(user)
                    .amount(amount)
                    .build();
            paymentRepository.save(payment);
            
            log.info("결제 생성: orderId={}, status=PENDING", request.getOrderId());
            
            try {
                // 3. Call Toss API (Blocking call, efficient on Virtual Threads)
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

                // 4. Record Ledger (Transactional)
                ledgerService.recordCharge(user, response.getTotalAmount(), response.getPaymentKey(), response.getOrderId());
                
                // 5. Payment 상태 업데이트 (CONFIRMED)
                payment.confirm(response.getPaymentKey());
                log.info("결제 승인 완료: orderId={}, paymentKey={}", request.getOrderId(), response.getPaymentKey());
                
                // 6. 일일 한도 업데이트
                paymentValidator.updateDailyLimit(user.getId(), response.getTotalAmount());
                
            } catch (Exception e) {
                // 7. 실패 시 Payment 상태 업데이트 (FAILED)
                payment.fail(e.getMessage());
                log.error("결제 실패: orderId={}, reason={}", request.getOrderId(), e.getMessage());
                throw e;
            }
        } finally {
            clearContext();
        }
    }
}
