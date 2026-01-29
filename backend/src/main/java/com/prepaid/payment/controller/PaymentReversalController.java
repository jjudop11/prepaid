package com.prepaid.payment.controller;

import com.prepaid.auth.annotation.CurrentUser;
import com.prepaid.domain.User;
import com.prepaid.ledger.service.LedgerService;
import com.prepaid.payment.dto.PaymentReversalRequest;
import com.prepaid.payment.dto.PaymentReversalResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 결제 취소 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payment Reversal", description = "결제 취소 API")
public class PaymentReversalController {
    
    private final LedgerService ledgerService;
    
    /**
     * 결제 취소
     * POST /api/payments/{paymentId}/reverse
     */
    @Operation(summary = "결제 취소", description = "사용한 결제를 취소하고 잔액을 복구합니다.")
    @PostMapping("/{paymentId}/reverse")
    public ResponseEntity<PaymentReversalResponse> reversePayment(
        @PathVariable Long paymentId,
        @RequestBody PaymentReversalRequest request,
        @CurrentUser User user
    ) {
        log.info("결제 취소 요청: paymentId={}, userId={}, reason={}", 
            paymentId, user.getId(), request.reason());
        
        Long reversedAmount = ledgerService.reverseUse(paymentId, user, request.reason());
        
        return ResponseEntity.ok(new PaymentReversalResponse(
            paymentId,
            reversedAmount,
            reversedAmount + "원이 복구되었습니다"
        ));
    }
}
