package com.prepaid.payment.service;

import com.prepaid.common.exception.ErrorCode;
import com.prepaid.common.exception.specific.InvalidAmountException;
import com.prepaid.domain.User;
import com.prepaid.ledger.service.LedgerService;
import com.prepaid.payment.dto.RefundRequest;
import com.prepaid.payment.dto.TossPaymentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * 환불 서비스
 * - Toss API 환불 요청
 * - 지갑 잔액 차감
 * - 환불 원장 기록
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefundService {

    private final RestClient tossRestClient;
    private final LedgerService ledgerService;

    @Value("${payment.refund.period-days:7}")
    private int refundPeriodDays;

    /**
     * 환불 처리
     */
    @Transactional
    public void processRefund(User user, RefundRequest request) {
        // 1. 금액 검증
        if (request.amount() == null || request.amount() <= 0) {
            throw new InvalidAmountException(ErrorCode.INVALID_REFUND_AMOUNT);
        }

        // 2. Toss API 환불 요청
        log.info("Toss 환불 요청: orderId={}, amount={}", request.orderId(), request.amount());
        
        // TODO: 실제 Toss API 호출 (현재는 시뮬레이션)
        // TossRefundResponse response = callTossRefundApi(request);
        
        // 3. 원장에 환불 기록
        ledgerService.recordRefund(user, request.amount(), request.orderId(), request.cancelReason());
        
        log.info("환불 완료: userId={}, orderId={}, amount={}", 
                user.getId(), request.orderId(), request.amount());
    }

    /**
     * 환불 가능 여부 확인
     */
    public boolean canRefund(LocalDateTime chargedAt) {
        LocalDateTime now = LocalDateTime.now();
        long daysSinceCharge = ChronoUnit.DAYS.between(chargedAt, now);
        return daysSinceCharge <= refundPeriodDays;
    }

    /**
     * Toss API 환불 호출 (실제 구현)
     */
    private TossPaymentResponse callTossRefundApi(RefundRequest request) {
        return tossRestClient.post()
                .uri("/cancel")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(TossPaymentResponse.class);
    }
}
