package com.prepaid.wallet.dto;

import com.prepaid.ledger.domain.LedgerEntry;

import java.time.LocalDateTime;

/**
 * 거래 상세 DTO (단순화 버전)
 */
public record TransactionDetailDTO(
    Long id,
    String txType,
    Long amount,
    String referenceId,
    String memo,
    String status,
    LocalDateTime createdAt
) {
    public static TransactionDetailDTO from(LedgerEntry entry, Long amount) {
        return new TransactionDetailDTO(
            entry.getId(),
            entry.getTxType().name(),
            amount,
            entry.getReferenceId(),
            entry.getMemo(),
            entry.getStatus().name(),
            entry.getCreatedAt()
        );
    }
}
