package com.prepaid.ledger.domain;

import com.prepaid.domain.Wallet;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ledger_entries")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class LedgerEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    @Enumerated(EnumType.STRING)
    @Column(name = "tx_type_code", nullable = false)
    private TxType txType; // CHARGE, USE...

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private LedgerStatus status = LedgerStatus.POSTED;

    @Column(name = "reference_id")
    private String referenceId; // orderId

    @Column(name = "idempotency_key", unique = true, nullable = false)
    private String idempotencyKey; // paymentKey

    @Enumerated(EnumType.STRING)
    @Column(name = "bucket_type")
    private BucketType bucketType; // PAID, FREE

    private Long originEntryId;
    private Long reversedEntryId;
    private String memo;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

}
