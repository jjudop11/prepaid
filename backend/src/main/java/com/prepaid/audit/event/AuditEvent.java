package com.prepaid.audit.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Kafka로 발행할 감사 이벤트
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditEvent {
    private Long userId;
    private String action;
    private Long amount;
    private String ipAddress;
    private String userAgent;
    private String result;
    private String errorMessage;
    private String referenceId;
    private LocalDateTime timestamp;

    public static AuditEvent success(Long userId, String action, Long amount, String referenceId, String ipAddress, String userAgent) {
        return AuditEvent.builder()
                .userId(userId)
                .action(action)
                .amount(amount)
                .referenceId(referenceId)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .result("SUCCESS")
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static AuditEvent failed(Long userId, String action, Long amount, String errorMessage, String ipAddress, String userAgent) {
        return AuditEvent.builder()
                .userId(userId)
                .action(action)
                .amount(amount)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .result("FAILED")
                .errorMessage(errorMessage)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
