package com.prepaid.event.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 모든 지갑 관련 이벤트의 기본 클래스
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class WalletEvent {
    /**
     * 멱등성을 위한 이벤트 고유 ID
     */
    private String eventId = UUID.randomUUID().toString();

    /**
     * 사용자 ID
     */
    private Long userId;

    /**
     * 이벤트 타입
     */
    private EventType eventType;

    /**
     * 이벤트 발생 시간
     */
    private LocalDateTime occurredAt = LocalDateTime.now();

    /**
     * 추가 메타데이터
     */
    private Map<String, Object> metadata = new HashMap<>();

    /**
     * 이벤트 타입과 사용자 ID로 생성
     */
    protected WalletEvent(Long userId, EventType eventType) {
        this.userId = userId;
        this.eventType = eventType;
        this.occurredAt = LocalDateTime.now();
        this.eventId = UUID.randomUUID().toString();
        this.metadata = new HashMap<>();
    }
}
