package com.prepaid.event.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 충전 완료 이벤트
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ChargeCompletedEvent extends WalletEvent {
    /**
     * 충전 금액 (원 단위)
     */
    private Long amount;

    /**
     * 충전 후 잔액 (원 단위)
     */
    private Long newBalance;

    /**
     * Toss 결제키
     */
    private String paymentKey;

    /**
     * 주문 ID
     */
    private String orderId;

    public ChargeCompletedEvent(Long userId, Long amount, Long newBalance, String paymentKey, String orderId) {
        super(userId, EventType.CHARGE_COMPLETED);
        this.amount = amount;
        this.newBalance = newBalance;
        this.paymentKey = paymentKey;
        this.orderId = orderId;
    }
}
