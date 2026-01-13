package com.prepaid.notification.service;

import com.prepaid.notification.dto.NotificationDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SSE 연결을 관리하는 스레드 안전 서비스
 */
@Slf4j
@Service
public class SseConnectionManager {

    /**
     * 사용자 ID -> SSE Emitter 매핑
     * ConcurrentHashMap으로 스레드 안전성 보장
     */
    private final Map<Long, SseEmitter> connections = new ConcurrentHashMap<>();

    /**
     * 기본 타임아웃: 30분
     */
    private static final long TIMEOUT = 30 * 60 * 1000L;

    /**
     * SSE 연결 추가
     * 
     * @param userId 사용자 ID
     * @return 생성된 SseEmitter
     */
    public SseEmitter addConnection(Long userId) {
        // 기존 연결이 있으면 제거
        removeConnection(userId);

        SseEmitter emitter = new SseEmitter(TIMEOUT);

        // 완료 시 연결 제거
        emitter.onCompletion(() -> {
            log.info("SSE 연결 완료: userId={}", userId);
            connections.remove(userId);
        });

        // 타임아웃 시 연결 제거
        emitter.onTimeout(() -> {
            log.info("SSE 연결 타임아웃: userId={}", userId);
            connections.remove(userId);
        });

        // 에러 발생 시 연결 제거
        emitter.onError(ex -> {
            log.error("SSE 연결 에러: userId={}", userId, ex);
            connections.remove(userId);
        });

        connections.put(userId, emitter);
        log.info("SSE 연결 추가: userId={}, 현재 연결 수={}", userId, connections.size());

        return emitter;
    }

    /**
     * SSE 연결 제거
     * 
     * @param userId 사용자 ID
     */
    public void removeConnection(Long userId) {
        SseEmitter emitter = connections.remove(userId);
        if (emitter != null) {
            emitter.complete();
            log.info("SSE 연결 제거: userId={}", userId);
        }
    }

    /**
     * 특정 사용자에게 알림 전송
     * 
     * @param userId       사용자 ID
     * @param notification 알림 DTO
     */
    public void sendToUser(Long userId, NotificationDto notification) {
        SseEmitter emitter = connections.get(userId);

        if (emitter == null) {
            log.debug("SSE 연결 없음: userId={}, eventId={}", userId, notification.getEventId());
            return;
        }

        try {
            emitter.send(SseEmitter.event()
                    .name("notification")
                    .data(notification));

            log.info("알림 전송 성공: userId={}, eventId={}, type={}",
                    userId, notification.getEventId(), notification.getEventType());
        } catch (IOException e) {
            log.error("알림 전송 실패: userId={}, eventId={}", userId, notification.getEventId(), e);
            // 전송 실패 시 연결 제거
            removeConnection(userId);
        }
    }

    /**
     * Heartbeat 전송 (연결 유지)
     * 
     * @param userId 사용자 ID
     */
    public void sendHeartbeat(Long userId) {
        SseEmitter emitter = connections.get(userId);

        if (emitter == null) {
            return;
        }

        try {
            emitter.send(SseEmitter.event()
                    .name("heartbeat")
                    .data("ping"));
        } catch (IOException e) {
            log.error("Heartbeat 전송 실패: userId={}", userId, e);
            removeConnection(userId);
        }
    }

    /**
     * 현재 활성 연결 수 반환
     */
    public int getActiveConnectionCount() {
        return connections.size();
    }
}
