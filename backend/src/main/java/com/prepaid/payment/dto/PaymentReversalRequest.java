package com.prepaid.payment.dto;

/**
 * 결제 취소 요청 DTO
 */
public record PaymentReversalRequest(
    String reason  // 취소 사유
) {
}
