package com.prepaid.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import com.prepaid.ledger.domain.BucketType;

@Entity
@Table(name = "wallets")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

    @Column(nullable = false)
    @Builder.Default
    private Long balancePaid = 0L;

    @Column(nullable = false)
    @Builder.Default
    private Long balanceFree = 0L;

    @Version
    @Builder.Default
    private Long version = 0L;

    @Column(nullable = false)
    @Builder.Default
    private String status = "ACTIVE";

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // 파생된 총 잔액
    public Long getBalance() {
        return balancePaid + balanceFree;
    }

    // 특정 잔액 버킷 업데이트
    public void addBalance(Long amount, BucketType type) {
        if (type == BucketType.FREE) {
            this.balanceFree += amount;
        } else {
            this.balancePaid += amount;
        }
    }
}
