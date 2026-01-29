package com.prepaid.ledger.domain;

import com.prepaid.domain.Wallet;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "charge_lots")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ChargeLot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    @Enumerated(EnumType.STRING)
    @Column(name = "bucket_type", nullable = false)
    private BucketType bucketType; // PAID, FREE

    @Column(name = "original_entry_id", nullable = false, unique = true)
    private Long originalEntryId;

    @Column(name = "amount_total", nullable = false)
    private Long amountTotal;

    @Column(name = "amount_remaining", nullable = false)
    private Long amountRemaining;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    public void decreaseRemaining(Long amount) {
        if (this.amountRemaining < amount) {
            throw new RuntimeException("ChargeLot insufficient amount");
        }
        this.amountRemaining -= amount;
    }

    /**
     * 잔액 복구 (사용 취소 시)
     */
    public void increaseRemaining(Long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("복구 금액은 0보다 커야 합니다");
        }
        
        Long newRemaining = this.amountRemaining + amount;
        if (newRemaining > this.amountTotal) {
            throw new IllegalStateException(
                String.format("복구 후 잔액이 원래 금액을 초과할 수 없습니다. total=%d, current=%d, adding=%d", 
                    amountTotal, amountRemaining, amount)
            );
        }
        
        this.amountRemaining = newRemaining;
    }

    /**
     * 포인트 만료 처리
     */
    public void expire() {
        this.amountRemaining = 0L;
    }
}
