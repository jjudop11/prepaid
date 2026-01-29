package com.prepaid.wallet.dto;

import com.prepaid.ledger.domain.LedgerEntry;

import java.time.LocalDateTime;

/**
 * 거래 내역 DTO
 */
public record TransactionDTO(
    Long id,
    String txType,          // CHARGE, USE, REFUND, etc
    Long amount,            // 거래 금액 (향후 서비스 레이어에서 계산)
    String referenceId,     // 주문 ID 등
    String memo,
    LocalDateTime createdAt
) {
    public static TransactionDTO from(LedgerEntry entry, Long amount) {
        return new TransactionDTO(
            entry.getId(),
            entry.getTxType().name(),
            amount,  // 서비스 레이어에서 전달받음
            entry.getReferenceId(),
            entry.getMemo(),
            entry.getCreatedAt()
        );
    }
}
