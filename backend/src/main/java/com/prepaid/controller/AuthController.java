package com.prepaid.controller;

import com.prepaid.auth.dto.AuthResponse;
import com.prepaid.auth.dto.LoginRequest;
import com.prepaid.auth.dto.SignupRequest;
import com.prepaid.auth.jwt.JwtProvider;
import com.prepaid.auth.service.TokenService;
import com.prepaid.auth.service.UserService;
import com.prepaid.auth.util.CookieUtils;
import com.prepaid.domain.User;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtProvider jwtProvider;
    private final TokenService tokenService;
    private final UserService userService;

    @Value("${jwt.expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token.expiration}")
    private long refreshTokenExpiration;

    /**
     * 회원가입
     */
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest request) {
        try {
            User user = userService.signup(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("회원가입이 완료되었습니다");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 일반 로그인
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response
    ) {
        try {
            // 1. 사용자 인증
            User user = userService.authenticateUser(request.getUsername(), request.getPassword());

            // 2. Authentication 객체 생성
            Authentication auth = new UsernamePasswordAuthenticationToken(
                    user.getUsername(),
                    null,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole()))
            );

            // 3. JWT 토큰 생성
            String accessToken = jwtProvider.createAccessToken(auth);
            String refreshToken = jwtProvider.createRefreshToken(auth);

            // 4. Refresh Token Redis에 저장
            tokenService.saveRefreshToken(user.getUsername(), refreshToken, refreshTokenExpiration);

            // 5. 쿠키 설정
            CookieUtils.addCookie(response, "accessToken", accessToken, (int) (accessTokenExpiration / 1000));
            CookieUtils.addCookie(response, "refreshToken", refreshToken, (int) (refreshTokenExpiration / 1000));

            // 6. 응답
            AuthResponse authResponse = AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .user(AuthResponse.UserDto.builder()
                            .id(user.getId())
                            .username(user.getUsername())
                            .email(user.getEmail())
                            .role(user.getRole())
                            .build())
                    .build();

            return ResponseEntity.ok(authResponse);

        } catch (IllegalStateException e) {
            // 계정 잠김
            return ResponseEntity.status(HttpStatus.LOCKED).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            // 인증 실패
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<Void> refresh(HttpServletRequest request, HttpServletResponse response) {
        // 1. Get Refresh Token from Cookie
        Optional<Cookie> cookie = CookieUtils.getCookie(request, "refreshToken");
        if (cookie.isEmpty()) {
            return ResponseEntity.status(401).build();
        }
        String refreshToken = cookie.get().getValue();

        // 2. Validate Token
        if (!jwtProvider.validateToken(refreshToken)) {
            return ResponseEntity.status(401).build();
        }

        // 3. Check Redis for Rotation/Validation
        Authentication auth = jwtProvider.getAuthentication(refreshToken);
        String username = auth.getName();
        String savedToken = tokenService.getRefreshToken(username);

        if (savedToken == null || !savedToken.equals(refreshToken)) {
            return ResponseEntity.status(401).build(); // Invalid or Rotated
        }

        // 4. Generate New Tokens
        String newAccessToken = jwtProvider.createAccessToken(auth);
        String newRefreshToken = jwtProvider.createRefreshToken(auth);

        // 5. Rotate in Redis
        tokenService.saveRefreshToken(username, newRefreshToken, refreshTokenExpiration);

        // 6. Set Cookies
        CookieUtils.addCookie(response, "accessToken", newAccessToken, (int) (accessTokenExpiration / 1000));
        CookieUtils.addCookie(response, "refreshToken", newRefreshToken, (int) (refreshTokenExpiration / 1000));

        return ResponseEntity.ok().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        // 1. Blacklist Access Token (if valid)
        Optional<Cookie> accessCookie = CookieUtils.getCookie(request, "accessToken");
        if (accessCookie.isPresent()) {
            String token = accessCookie.get().getValue();
            // Calculate remaining time, simplified for MVP (use fixed expiration or decode
            // exp)
            // Ideally parse JWT "exp" claim - "now". Here we just use default TTL or parse
            // it.
            // Let's rely on TokenService to verify existence, but to blacklist we strictly
            // need expiration.
            // We'll just set it to accessTokenExpiration default for now as a safe upper
            // bound.
            tokenService.addToBlacklist(token, accessTokenExpiration);
        }

        // 2. Delete Refresh Token from Redis
        Optional<Cookie> refreshCookie = CookieUtils.getCookie(request, "refreshToken");
        if (refreshCookie.isPresent() && jwtProvider.validateToken(refreshCookie.get().getValue())) {
            Authentication auth = jwtProvider.getAuthentication(refreshCookie.get().getValue());
            tokenService.deleteRefreshToken(auth.getName());
        }

        // 3. Clear Cookies
        CookieUtils.deleteCookie(request, response, "accessToken");
        CookieUtils.deleteCookie(request, response, "refreshToken");

        return ResponseEntity.ok().build();
    }
}
