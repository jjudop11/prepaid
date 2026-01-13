package com.prepaid.controller;

import com.prepaid.notification.service.SseConnectionManager;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * SSE(Server-Sent Events) 알림 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final SseConnectionManager sseConnectionManager;

    @Value("${jwt.secret}")
    private String jwtSecret;

    /**
     * SSE 스트림 구독 엔드포인트
     * EventSource API는 커스텀 헤더를 지원하지 않아 쿼리 파라미터로 토큰 전달
     * 
     * @param token JWT 액세스 토큰
     * @return SSE Emitter
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@RequestParam String token) {
        try {
            // JWT 토큰 검증 및 userId 추출
            Long userId = validateTokenAndExtractUserId(token);

            log.info("SSE 연결 요청: userId={}", userId);

            // SSE Emitter 생성 및 등록
            SseEmitter emitter = sseConnectionManager.addConnection(userId);

            // 연결 성공 메시지 전송
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data("SSE 연결됨"));

            return emitter;
        } catch (Exception e) {
            log.error("SSE 연결 실패", e);
            throw new RuntimeException("SSE 연결 실패: " + e.getMessage());
        }
    }

    /**
     * JWT 토큰 검증 및 userId 추출
     * 
     * @param token JWT 토큰
     * @return 사용자 ID
     */
    private Long validateTokenAndExtractUserId(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(jwtSecret.getBytes())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            // userId는 subject에 저장되어 있다고 가정
            String userIdStr = claims.getSubject();
            return Long.parseLong(userIdStr);
        } catch (Exception e) {
            log.error("JWT 토큰 검증 실패", e);
            throw new RuntimeException("유효하지 않은 토큰");
        }
    }

    /**
     * 현재 활성 SSE 연결 수 조회 (관리자용)
     */
    @GetMapping("/connections/count")
    public ResponseEntity<Integer> getConnectionCount() {
        return ResponseEntity.ok(sseConnectionManager.getActiveConnectionCount());
    }
}
