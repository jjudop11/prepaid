package com.prepaid.wallet.controller;

import com.prepaid.auth.annotation.CurrentUser;
import com.prepaid.domain.User;
import com.prepaid.ledger.domain.TxType;
import com.prepaid.wallet.dto.TransactionDTO;
import com.prepaid.wallet.dto.TransactionDetailDTO;
import com.prepaid.wallet.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * 거래 내역 API
 */
@Tag(name = "Transaction", description = "거래 내역 조회 API")
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {
    
    private final TransactionService transactionService;
    
    /**
     * 거래 내역 조회 (페이징, 필터링)
     */
    @Operation(summary = "거래 내역 조회", description = "사용자의 거래 내역을 조회합니다 (페이징, 필터링 지원)")
    @GetMapping
    public ResponseEntity<Page<TransactionDTO>> getTransactions(
        @CurrentUser User user,
        
        @Parameter(description = "거래 유형 (CHARGE, USE, REFUND, etc)")
        @RequestParam(required = false) TxType type,
        
        @Parameter(description = "시작 날짜 (YYYY-MM-DD)")
        @RequestParam(required = false) 
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        
        @Parameter(description = "종료 날짜 (YYYY-MM-DD)")
        @RequestParam(required = false) 
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
        
        @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) 
        Pageable pageable
    ) {
        Page<TransactionDTO> transactions = transactionService.getTransactions(
            user, type, startDate, endDate, pageable
        );
        return ResponseEntity.ok(transactions);
    }
    
    /**
     * 거래 상세 조회
     */
    @Operation(summary = "거래 상세 조회", description = "특정 거래의 상세 정보를 조회합니다")
    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionDetailDTO> getTransaction(
        @PathVariable Long transactionId,
        @CurrentUser User user
    ) {
        TransactionDetailDTO detail = transactionService.getTransactionDetail(transactionId, user);
        return ResponseEntity.ok(detail);
    }
}
