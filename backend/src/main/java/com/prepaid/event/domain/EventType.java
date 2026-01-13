package com.prepaid.event.domain;

/**
 * 지갑 이벤트 타입
 */
public enum EventType {
    /**
     * 충전 완료
     */
    CHARGE_COMPLETED,

    /**
     * 사용 완료
     */
    SPEND_COMPLETED,

    /**
     * 취소 완료
     */
    REVERSAL_COMPLETED
}
