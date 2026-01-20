package com.prepaid.controller;

import com.prepaid.audit.event.AuditEvent;
import com.prepaid.audit.service.AuditEventPublisher;
import com.prepaid.auth.jwt.JwtProvider;
import com.prepaid.common.exception.specific.UserNotFoundException;
import com.prepaid.common.idempotency.IdempotencyService;
import com.prepaid.domain.User;
import com.prepaid.payment.dto.PaymentConfirmRequest;
import com.prepaid.payment.service.PaymentService;
import com.prepaid.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.prepaid.auth.util.CookieUtils;
import com.prepaid.payment.dto.PaymentUseRequest;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

        private final PaymentService paymentService;
        private final com.prepaid.ledger.service.LedgerService ledgerService;
        private final UserRepository userRepository;
        private final JwtProvider jwtProvider;
        private final IdempotencyService idempotencyService;
        private final AuditEventPublisher auditEventPublisher;

        /**
         * 충전 확인
         * Idempotency-Key 헤더로 중복 요청 방지
         */
        @PostMapping("/confirm")
        public ResponseEntity<Void> confirmPayment(
                        @RequestHeader(value = "Idempotency-Key", required = true) String idempotencyKey,
                        @RequestBody PaymentConfirmRequest request,
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

                        // 3. 결제 확인 처리
                        paymentService.confirmPayment(targetUser, request);
                        
                        // 4. 성공 처리
                        idempotencyService.markCompleted(idempotencyKey, "SUCCESS");
                        
                        // 5. 감사 로그 발행
                        auditEventPublisher.publish(AuditEvent.success(
                                targetUser.getId(), "CHARGE", request.getAmount(),
                                request.getOrderId(), ipAddress, userAgent
                        ));
                        
                        return ResponseEntity.ok().build();
                } catch (Exception e) {
                        // 6. 실패 처리
                        idempotencyService.markFailed(idempotencyKey, e.getMessage());
                        
                        // 7. 감사 로그 발행 (실패)
                        if (targetUser != null) {
                                auditEventPublisher.publish(AuditEvent.failed(
                                        targetUser.getId(), "CHARGE", request.getAmount(),
                                        e.getMessage(), ipAddress, userAgent
                                ));
                        }
                        
                        throw e;
                }
        }

        /**
         * 잔액 사용
         * Idempotency-Key 헤더로 중복 요청 방지
         */
        @PostMapping("/use")
        public ResponseEntity<Void> useBalance(
                        @RequestHeader(value = "Idempotency-Key", required = true) String idempotencyKey,
                        @RequestBody PaymentUseRequest request,
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

                        // 3. 잔액 사용 처리
                        ledgerService.useBalance(targetUser, request.getAmount(), request.getMerchantUid());
                        
                        // 4. 성공 처리
                        idempotencyService.markCompleted(idempotencyKey, "SUCCESS");
                        
                        // 5. 감사 로그 발행
                        auditEventPublisher.publish(AuditEvent.success(
                                targetUser.getId(), "USE", request.getAmount(),
                                request.getMerchantUid(), ipAddress, userAgent
                        ));
                        
                        return ResponseEntity.ok().build();
                } catch (Exception e) {
                        // 6. 실패 처리
                        idempotencyService.markFailed(idempotencyKey, e.getMessage());
                        
                        // 7. 감사 로그 발행 (실패)
                        if (targetUser != null) {
                                auditEventPublisher.publish(AuditEvent.failed(
                                        targetUser.getId(), "USE", request.getAmount(),
                                        e.getMessage(), ipAddress, userAgent
                                ));
                        }
                        
                        throw e;
                }
        }

        /**
         * 클라이언트 IP 주소 추출
         */
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
