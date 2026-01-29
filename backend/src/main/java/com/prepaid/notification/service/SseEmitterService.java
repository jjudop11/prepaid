package com.prepaid.notification.service;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SSE(Server-Sent Events) 서비스
 * - 실시간 알림 전송
 * - 잔액 업데이트 알림
 */
@Slf4j
@Service
public class SseEmitterService {
    
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();
    
    // SSE 타임아웃: 30분
    private static final Long TIMEOUT = 30 * 60 * 1000L;
    
    /**
     * SSE 연결 생성
     */
    public SseEmitter createEmitter(Long userId) {
        SseEmitter emitter = new SseEmitter(TIMEOUT);
        
        // 연결 저장
        emitters.put(userId, emitter);
        log.info("SSE 연결 생성: userId={}", userId);
        
        // 연결 종료 시 제거
        emitter.onCompletion(() -> {
            emitters.remove(userId);
            log.info("SSE 연결 종료: userId={}", userId);
        });
        
        emitter.onTimeout(() -> {
            emitters.remove(userId);
            log.info("SSE 타임아웃: userId={}", userId);
        });
        
        emitter.onError((e) -> {
            emitters.remove(userId);
            log.error("SSE 에러: userId={}", userId, e);
        });
        
        // 연결 확인 이벤트 전송
        try {
            emitter.send(SseEmitter.event()
                .name("connected")
                .data(Map.of("message", "SSE 연결 성공", "timestamp", LocalDateTime.now())));
        } catch (IOException e) {
            log.error("초기 이벤트 전송 실패: userId={}", userId, e);
        }
        
        return emitter;
    }
    
    /**
     * 잔액 업데이트 알림 (Trace ID 포함)
     */
    public void sendBalanceUpdate(Long userId, Long newBalance) {
        SseEmitter emitter = emitters.get(userId);
        if (emitter == null) {
            log.debug("SSE 연결 없음: userId={}", userId);
            return;
        }
        
        // MDC에서 Trace ID 가져오기
        String traceId = MDC.get("traceId");
        String spanId = MDC.get("spanId");
        
        Map<String, Object> data = Map.of(
            "balance", newBalance,
            "timestamp", LocalDateTime.now(),
            "traceId", traceId != null ? traceId : "",
            "spanId", spanId != null ? spanId : ""
        );
        
        try {
            emitter.send(SseEmitter.event()
                .name("balance-update")
                .data(data));
            log.info("잔액 업데이트 알림 전송: userId={}, balance={}, traceId={}", 
                userId, newBalance, traceId);
        } catch (IOException e) {
            log.error("SSE 전송 실패: userId={}", userId, e);
            emitters.remove(userId);
        }
    }
    
    /**
     * 거래 완료 알림 (Trace ID 포함)
     */
    public void sendTransactionComplete(Long userId, String txType, Long amount) {
        SseEmitter emitter = emitters.get(userId);
        if (emitter == null) {
            log.debug("SSE 연결 없음: userId={}", userId);
            return;
        }
        
        // MDC에서 Trace ID 가져오기
        String traceId = MDC.get("traceId");
        
        Map<String, Object> data = Map.of(
            "txType", txType,
            "amount", amount,
            "timestamp", LocalDateTime.now(),
            "traceId", traceId != null ? traceId : ""
        );
        
        try {
            emitter.send(SseEmitter.event()
                .name("transaction-complete")
                .data(data));
            log.info("거래 완료 알림 전송: userId={}, txType={}, traceId={}", 
                userId, txType, traceId);
        } catch (IOException e) {
            log.error("SSE 전송 실패: userId={}", userId, e);
            emitters.remove(userId);
        }
    }
    
    /**
     * 연결된 사용자 수
     */
    public int getConnectedCount() {
        return emitters.size();
    }
}
