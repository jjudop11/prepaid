package com.prepaid.payment.controller;

import com.prepaid.auth.annotation.CurrentUser;
import com.prepaid.domain.User;
import com.prepaid.ledger.service.LedgerService;
import com.prepaid.payment.dto.ChargeCancelRequest;
import com.prepaid.payment.dto.ChargeCancelResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 충전 취소 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/charges")
@RequiredArgsConstructor
@Tag(name = "Charge Cancel", description = "충전 취소 API")
public class ChargeCancelController {
    
    private final LedgerService ledgerService;
    
    /**
     * 충전 취소
     * POST /api/charges/{chargeId}/cancel
     */
    @Operation(summary = "충전 취소", description = "특정 충전 건을 취소합니다. 이미 사용된 금액은 제외하고 남은 금액만 취소됩니다.")
    @PostMapping("/{chargeId}/cancel")
    public ResponseEntity<ChargeCancelResponse> cancelCharge(
        @PathVariable Long chargeId,
        @RequestBody ChargeCancelRequest request,
        @CurrentUser User user
    ) {
        log.info("충전 취소 요청: chargeId={}, userId={}, reason={}", 
            chargeId, user.getId(), request.reason());
        
        Long canceledAmount = ledgerService.cancelCharge(chargeId, user, request.reason());
        
        return ResponseEntity.ok(new ChargeCancelResponse(
            chargeId,
            canceledAmount,
            canceledAmount + "원이 취소되었습니다"
        ));
    }
}
