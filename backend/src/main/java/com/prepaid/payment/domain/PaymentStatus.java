package com.prepaid.payment.domain;

/**
 * 결제 상태
 */
public enum PaymentStatus {
    PENDING,      // 승인 대기 중
    CONFIRMED,    // 승인 완료
    FAILED,       // 실패
    CANCELLED     // 취소됨
}
