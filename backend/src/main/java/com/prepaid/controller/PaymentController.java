package com.prepaid.controller;

import com.prepaid.auth.jwt.JwtProvider;
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
import com.prepaid.payment.dto.PaymentUseRequest; // Added import for PaymentUseRequest

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

        private final PaymentService paymentService;
        private final com.prepaid.ledger.service.LedgerService ledgerService;
        private final UserRepository userRepository;
        private final JwtProvider jwtProvider; // Quick user fetch for MVP

        // 사용자 조회 (Simplified: 실제 앱에서는 ArgumentResolver 사용)
        @PostMapping("/confirm")
        public ResponseEntity<Void> confirmPayment(@RequestBody PaymentConfirmRequest request,
                        HttpServletRequest httpRequest) {
                String accessToken = CookieUtils.getCookie(httpRequest, "accessToken")
                                .map(Cookie::getValue).orElseThrow(() -> new RuntimeException("토큰이 없습니다."));

                Authentication auth = jwtProvider.getAuthentication(accessToken);
                // CustomOAuth2UserService에서는 주로 이메일을 Principal Name으로 저장함.
                // 이메일이 고유하다고 가정하고 사용자 조회.
                User targetUser = userRepository.findByEmail(auth.getName())
                                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

                paymentService.confirmPayment(targetUser, request);
                return ResponseEntity.ok().build();
        }

        @PostMapping("/use")
        public ResponseEntity<Void> useBalance(@RequestBody PaymentUseRequest request, HttpServletRequest httpRequest) {
                String accessToken = CookieUtils.getCookie(httpRequest, "accessToken")
                                .map(Cookie::getValue).orElseThrow(() -> new RuntimeException("토큰이 없습니다."));

                Authentication auth = jwtProvider.getAuthentication(accessToken);
                User targetUser = userRepository.findByEmail(auth.getName())
                                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

                // LedgerService 직접 호출 vs PaymentService 경유
                // "결제 사용"은 Toss 연동(PaymentService)과는 별개의 내부 로직이므로 LedgerService 사용.
                // 장기적으로는 PaymentService가 Facade 역할을 할 수도 있겠지만, 현재는 "포인트 사용"의 명확성을 위해 직접 주입함.
                ledgerService.useBalance(targetUser, request.getAmount(), request.getMerchantUid());

                return ResponseEntity.ok().build();
        }
}
