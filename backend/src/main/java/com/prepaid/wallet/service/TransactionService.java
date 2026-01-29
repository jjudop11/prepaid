package com.prepaid.wallet.service;

import com.prepaid.common.exception.specific.UnauthorizedException;
import com.prepaid.common.exception.specific.WalletNotFoundException;
import com.prepaid.domain.User;
import com.prepaid.domain.Wallet;
import com.prepaid.ledger.domain.LedgerEntry;
import com.prepaid.ledger.domain.TxType;
import com.prepaid.ledger.repository.LedgerEntryRepository;
import com.prepaid.repository.WalletRepository;
import com.prepaid.wallet.dto.TransactionDTO;
import com.prepaid.wallet.dto.TransactionDetailDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static com.prepaid.common.logging.LoggingUtils.*;

/**
 * 거래 내역 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {
    
    private final WalletRepository walletRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    
    /**
     * 거래 내역 조회 (페이징)
     */
    @Transactional(readOnly = true)
    public Page<TransactionDTO> getTransactions(
        User user,
        TxType type,
        LocalDate startDate,
        LocalDate endDate,
        Pageable pageable
    ) {
        setUserContext(user.getId());
        
        try {
        Wallet wallet = walletRepository.findByUserId(user.getId())
            .orElseThrow(() -> new WalletNotFoundException());
        
        LocalDateTime start = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime end = endDate != null ? endDate.atTime(LocalTime.MAX) : null;
        
        // LedgerEntry 조회 (조건 필터링)
        Page<LedgerEntry> entries;
        
        if (type != null && start != null && end != null) {
            entries = ledgerEntryRepository.findByWalletIdAndTxTypeAndCreatedAtBetween(
                wallet.getId(), type, start, end, pageable);
        } else if (type != null) {
            entries = ledgerEntryRepository.findByWalletIdAndTxType(
                wallet.getId(), type, pageable);
        } else if (start != null && end != null) {
            entries = ledgerEntryRepository.findByWalletIdAndCreatedAtBetween(
                wallet.getId(), start, end, pageable);
        } else {
            entries = ledgerEntryRepository.findByWalletId(wallet.getId(), pageable);
        }
        
        // DTO 변환 (금액은 txType에 따라 임시로 1000L로 설정, 추후 개선 필요)
        return entries.map(entry -> {
            // TODO: 실제 금액 계산 로직 구현 필요 (LedgerLine 조회)
            Long amount = 1000L;  // 임시 값
            return TransactionDTO.from(entry, amount);
        });
        } finally {
            clearContext();
        }
    }
    
    /**
     * 거래 상세 조회
     */
    @Transactional(readOnly = true)
    public TransactionDetailDTO getTransactionDetail(Long entryId, User user) {
        setUserContext(user.getId());
        
        try {
        LedgerEntry entry = ledgerEntryRepository.findById(entryId)
            .orElseThrow(() -> new IllegalArgumentException("거래 내역을 찾을 수 없습니다"));
        
        // 권한 확인 (본인 거래만 조회 가능)
        if (!entry.getWallet().getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException();
        }
        
        // TODO: 실제 금액 계산
        Long amount = 1000L;  // 임시 값
        return TransactionDetailDTO.from(entry, amount);
        } finally {
            clearContext();
        }
    }
}


