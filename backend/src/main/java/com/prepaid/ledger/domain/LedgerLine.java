package com.prepaid.ledger.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ledger_lines")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class LedgerLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entry_id", nullable = false)
    private LedgerEntry entry;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_code", nullable = false)
    private AccountCode accountCode;

    @Column(name = "amount_signed", nullable = false)
    private Long amountSigned;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
