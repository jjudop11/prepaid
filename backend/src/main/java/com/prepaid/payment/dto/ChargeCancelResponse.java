package com.prepaid.payment.dto;

/**
 * 충전 취소 응답 DTO
 */
public record ChargeCancelResponse(
    Long chargeId,        // 충전 ID
    Long canceledAmount,  // 취소된 금액
    String message        // 응답 메시지
) {
}
