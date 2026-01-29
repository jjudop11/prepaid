package com.prepaid.ledger.repository;

import com.prepaid.ledger.domain.LedgerEntry;
import com.prepaid.ledger.domain.TxType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, Long> {
    
    /**
     * 특정 거래가 이미 취소되었는지 확인
     */
    boolean existsByTxTypeAndReferenceId(TxType txType, String referenceId);
    
    /**
     * 지갑 ID로 거래 내역 조회 (페이징)
     */
    Page<LedgerEntry> findByWalletIdOrderByCreatedAtDesc(Long walletId, Pageable pageable);
    
    /**
     * 편의 메서드 (정렬 기본값 포함)
     */
    default Page<LedgerEntry> findByWalletId(Long walletId, Pageable pageable) {
        return findByWalletIdOrderByCreatedAtDesc(walletId, pageable);
    }
    
    /**
     * 거래 유형 필터
     */
    Page<LedgerEntry> findByWalletIdAndTxTypeOrderByCreatedAtDesc(Long walletId, TxType txType, Pageable pageable);
    
    default Page<LedgerEntry> findByWalletIdAndTxType(Long walletId, TxType txType, Pageable pageable) {
        return findByWalletIdAndTxTypeOrderByCreatedAtDesc(walletId, txType, pageable);
    }
    
    /**
     * 기간 필터
     */
    Page<LedgerEntry> findByWalletIdAndCreatedAtBetweenOrderByCreatedAtDesc(
        Long walletId, LocalDateTime start, LocalDateTime end, Pageable pageable);
    
    default Page<LedgerEntry> findByWalletIdAndCreatedAtBetween(
        Long walletId, LocalDateTime start, LocalDateTime end, Pageable pageable) {
        return findByWalletIdAndCreatedAtBetweenOrderByCreatedAtDesc(walletId, start, end, pageable);
    }
    
    /**
     * 거래 유형 + 기간 필터
     */
    Page<LedgerEntry> findByWalletIdAndTxTypeAndCreatedAtBetweenOrderByCreatedAtDesc(
        Long walletId, TxType txType, LocalDateTime start, LocalDateTime end, Pageable pageable);
    
    default Page<LedgerEntry> findByWalletIdAndTxTypeAndCreatedAtBetween(
        Long walletId, TxType txType, LocalDateTime start, LocalDateTime end, Pageable pageable) {
        return findByWalletIdAndTxTypeAndCreatedAtBetweenOrderByCreatedAtDesc(walletId, txType, start, end, pageable);
    }
}
