package com.prepaid.payment.domain;

import com.prepaid.domain.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 결제 엔티티
 * - 결제 승인 대기 상태 관리
 * - 결제 이력 추적
 */
@Entity
@Table(name = "payments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Payment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 100)
    private String orderId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false)
    private Long amount;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;
    
    @Column(length = 255)
    private String paymentKey;      // Toss 결제 키
    
    @Column(columnDefinition = "TEXT")
    private String failureReason;   // 실패 사유
    
    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    private LocalDateTime confirmedAt;
    private LocalDateTime failedAt;
    private LocalDateTime cancelledAt;
    
    /**
     * 결제 승인 완료
     */
    public void confirm(String paymentKey) {
        this.status = PaymentStatus.CONFIRMED;
        this.paymentKey = paymentKey;
        this.confirmedAt = LocalDateTime.now();
    }
    
    /**
     * 결제 실패 처리
     */
    public void fail(String reason) {
        this.status = PaymentStatus.FAILED;
        this.failureReason = reason;
        this.failedAt = LocalDateTime.now();
    }
    
    /**
     * 결제 취소 처리
     */
    public void cancel(String reason) {
        if (this.status != PaymentStatus.CONFIRMED) {
            throw new IllegalStateException("승인된 결제만 취소 가능합니다");
        }
        this.status = PaymentStatus.CANCELLED;
        this.failureReason = reason;
        this.cancelledAt = LocalDateTime.now();
    }
}
