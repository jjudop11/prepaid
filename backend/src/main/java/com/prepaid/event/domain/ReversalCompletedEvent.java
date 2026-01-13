package com.prepaid.event.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 취소 완료 이벤트
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ReversalCompletedEvent extends WalletEvent {
    /**
     * 취소된 금액 (원 단위)
     */
    private Long reversedAmount;

    /**
     * 취소 후 잔액 (원 단위)
     */
    private Long newBalance;

    /**
     * 원본 거래 ID
     */
    private Long originalEntryId;

    /**
     * 취소 사유
     */
    private String reason;

    public ReversalCompletedEvent(Long userId, Long reversedAmount, Long newBalance, Long originalEntryId,
            String reason) {
        super(userId, EventType.REVERSAL_COMPLETED);
        this.reversedAmount = reversedAmount;
        this.newBalance = newBalance;
        this.originalEntryId = originalEntryId;
        this.reason = reason;
    }
}
