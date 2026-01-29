package com.prepaid.payment.dto;

/**
 * 충전 취소 요청 DTO
 */
public record ChargeCancelRequest(
    String reason  // 취소 사유
) {
}
