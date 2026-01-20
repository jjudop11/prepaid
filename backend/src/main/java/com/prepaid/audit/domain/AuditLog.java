package com.prepaid.audit.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 감사 로그 Entity
 * PostgreSQL에 30일간 보관
 */
@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_user_timestamp", columnList = "user_id,timestamp"),
    @Index(name = "idx_action", columnList = "action"),
    @Index(name = "idx_timestamp", columnList = "timestamp")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 사용자 ID
     */
    @Column(nullable = false)
    private Long userId;

    /**
     * 액션 타입: CHARGE, USE, REFUND
     */
    @Column(nullable = false, length = 20)
    private String action;

    /**
     * 금액
     */
    @Column(nullable = false)
    private Long amount;

    /**
     * IP 주소
     */
    @Column(length = 50)
    private String ipAddress;

    /**
     * User-Agent
     */
    @Column(length = 500)
    private String userAgent;

    /**
     * 결과: SUCCESS, FAILED
     */
    @Column(nullable = false, length = 10)
    private String result;

    /**
     * 에러 메시지 (실패 시)
     */
    @Column(length = 1000)
    private String errorMessage;

    /**
     * 참조 ID (orderId, merchantUid 등)
     */
    @Column(length = 100)
    private String referenceId;

    /**
     * 발생 시각
     */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }
}
