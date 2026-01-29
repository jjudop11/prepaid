package com.prepaid.wallet.controller;

import com.prepaid.auth.annotation.CurrentUser;
import com.prepaid.domain.User;
import com.prepaid.wallet.dto.WalletBalanceDTO;
import com.prepaid.wallet.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 지갑 API
 */
@Tag(name = "Wallet", description = "지갑 관리 API")
@RestController
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
public class WalletController {
    
    private final WalletService walletService;
    
    /**
     * 잔액 조회
     */
    @Operation(summary = "잔액 조회", description = "사용자의 현재 잔액을 조회합니다")
    @GetMapping("/balance")
    public ResponseEntity<WalletBalanceDTO> getBalance(@CurrentUser User user) {
        WalletBalanceDTO balance = walletService.getBalance(user);
        return ResponseEntity.ok(balance);
    }
}
