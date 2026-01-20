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
     * 포인트 만료 처리
     */
    public void expire() {
        this.amountRemaining = 0L;
    }
}
