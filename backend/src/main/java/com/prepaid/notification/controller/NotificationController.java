package com.prepaid.notification.controller;

import com.prepaid.auth.annotation.CurrentUser;
import com.prepaid.domain.User;
import com.prepaid.notification.service.SseEmitterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 실시간 알림 API
 */
@Tag(name = "Notification", description = "실시간 알림 API")
@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    
    private final SseEmitterService sseEmitterService;
    
    /**
     * SSE 구독 (실시간 알림 연결)
     */
    @Operation(summary = "실시간 알림 구독", description = "SSE를 통해 실시간 잔액 및 거래 알림을 받습니다")
    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@CurrentUser User user) {
        log.info("SSE 구독 요청: userId={}", user.getId());
        return sseEmitterService.createEmitter(user.getId());
    }
    
    /**
     * 연결된 사용자 수 조회 (모니터링용)
     */
    @Operation(summary = "연결된 사용자 수", description = "현재 SSE에 연결된 사용자 수를 조회합니다")
    @GetMapping("/connected-count")
    public int getConnectedCount() {
        return sseEmitterService.getConnectedCount();
    }
}
