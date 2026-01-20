package com.prepaid.controller;

import com.prepaid.audit.event.AuditEvent;
import com.prepaid.audit.service.AuditEventPublisher;
import com.prepaid.auth.jwt.JwtProvider;
import com.prepaid.common.dto.ErrorResponse;
import com.prepaid.common.exception.specific.UserNotFoundException;
import com.prepaid.common.idempotency.IdempotencyService;
import com.prepaid.domain.User;
import com.prepaid.payment.dto.RefundRequest;
import com.prepaid.payment.service.RefundService;
import com.prepaid.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.prepaid.auth.util.CookieUtils;

/**
 * 환불 컨트롤러
 */
@Tag(name = "Refund", description = "환불 관리 API")
@RestController
@RequestMapping("/api/refunds")
@RequiredArgsConstructor
public class RefundController {

    private final RefundService refundService;
    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final IdempotencyService idempotencyService;
    private final AuditEventPublisher auditEventPublisher;

    @Operation(summary = "환불 요청", description = "결제 취소 및 환불 처리")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "환불 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "중복 요청",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<Void> refund(
            @Parameter(description = "멱등성 키", required = true)
            @RequestHeader(value = "Idempotency-Key", required = true) String idempotencyKey,
            @RequestBody RefundRequest request,
            HttpServletRequest httpRequest) {

        // 1. 멱등성 체크
        idempotencyService.startProcessing(idempotencyKey);

        User targetUser = null;
        String ipAddress = getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        try {
            // 2. 사용자 조회
            String accessToken = CookieUtils.getCookie(httpRequest, "accessToken")
                    .map(Cookie::getValue)
                    .orElseThrow(() -> new RuntimeException("토큰이 없습니다."));

            Authentication auth = jwtProvider.getAuthentication(accessToken);
            targetUser = userRepository.findByEmail(auth.getName())
                    .orElseThrow(() -> new UserNotFoundException());

            // 3. 환불 처리
            refundService.processRefund(targetUser, request);

            // 4. 성공 처리
            idempotencyService.markCompleted(idempotencyKey, "SUCCESS");

            // 5. 감사 로그 발행
            auditEventPublisher.publish(AuditEvent.success(
                    targetUser.getId(), "REFUND", request.amount(),
                    request.orderId(), ipAddress, userAgent
            ));

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            // 6. 실패 처리
            idempotencyService.markFailed(idempotencyKey, e.getMessage());

            // 7. 감사 로그 발행 (실패)
            if (targetUser != null) {
                auditEventPublisher.publish(AuditEvent.failed(
                        targetUser.getId(), "REFUND", request.amount(),
                        e.getMessage(), ipAddress, userAgent
                ));
            }

            throw e;
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
