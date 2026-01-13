package com.prepaid.notification.dto;

import com.prepaid.event.domain.EventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 클라이언트로 전송되는 알림 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDto {
    /**
     * 이벤트 ID (멱등성 체크용)
     */
    private String eventId;

    /**
     * 이벤트 타입
     */
    private EventType eventType;

    /**
     * 알림 제목
     */
    private String title;

    /**
     * 알림 메시지
     */
    private String message;

    /**
     * 발생 시간
     */
    private LocalDateTime occurredAt;

    /**
     * 알림 타입 (success, error, info)
     */
    private String notificationType;
}
