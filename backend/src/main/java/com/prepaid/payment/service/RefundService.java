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

import static com.prepaid.common.logging.LoggingUtils.*;

/**
 * 환불/인출 서비스
 * - 순수한 잔액 인출 처리 (은행 계좌로 출금)
 * - 충전 취소가 필요한 경우 ChargeCancelController 사용
 * 
 * Note: Toss API 환불 연동은 향후 구현 예정
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
        // MDC 컨텍스트 설정
        setUserContext(user.getId());
        setTransactionContext("REFUND", request.amount());
        setOrderContext(request.orderId());
        
        try {
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
        
        log.info("환불 완료");
        } finally {
            clearContext();
        }
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
