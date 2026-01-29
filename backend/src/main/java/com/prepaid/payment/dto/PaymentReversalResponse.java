package com.prepaid.payment.dto;

/**
 * 결제 취소 응답 DTO
 */
public record PaymentReversalResponse(
    Long paymentId,       // 결제 ID
    Long reversedAmount,  // 복구된 금액
    String message        // 응답 메시지
) {
}
