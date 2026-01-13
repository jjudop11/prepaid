package com.prepaid.event.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 사용 완료 이벤트
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SpendCompletedEvent extends WalletEvent {
    /**
     * 사용 금액 (원 단위, 음수)
     */
    private Long amount;

    /**
     * 사용 후 잔액 (원 단위)
     */
    private Long newBalance;

    /**
     * 참조 ID (주문 ID 등)
     */
    private String referenceId;

    /**
     * 사용 설명
     */
    private String description;

    public SpendCompletedEvent(Long userId, Long amount, Long newBalance, String referenceId, String description) {
        super(userId, EventType.SPEND_COMPLETED);
        this.amount = amount;
        this.newBalance = newBalance;
        this.referenceId = referenceId;
        this.description = description;
    }
}
