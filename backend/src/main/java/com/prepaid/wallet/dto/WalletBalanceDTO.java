package com.prepaid.wallet.dto;

import java.time.LocalDateTime;

/**
 * 지갑 잔액 응답 DTO
 */
public record WalletBalanceDTO(
    Long totalBalance,      // 총 잔액
    Long paidBalance,       // 유료 잔액 (실제 결제한 금액)
    Long freeBalance,       // 무료 잔액 (포인트/리워드)
    LocalDateTime updatedAt // 마지막 업데이트 시간
) {
}
