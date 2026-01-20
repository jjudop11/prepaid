package com.prepaid.payment.dto;

/**
 * 환불 요청 DTO
 */
public record RefundRequest(
    String orderId,
    Long amount,
    String cancelReason
) {
    public RefundRequest {
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("주문 ID는 필수입니다.");
        }
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("환불 금액은 0보다 커야 합니다.");
        }
    }
}
