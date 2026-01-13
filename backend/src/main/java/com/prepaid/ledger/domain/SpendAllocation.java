package com.prepaid.ledger.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "spend_allocations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class SpendAllocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "spend_entry_id", nullable = false)
    private LedgerEntry spendEntry;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "charge_lot_id", nullable = false)
    private ChargeLot chargeLot;

    @Column(name = "amount_consumed", nullable = false)
    private Long amountConsumed;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
