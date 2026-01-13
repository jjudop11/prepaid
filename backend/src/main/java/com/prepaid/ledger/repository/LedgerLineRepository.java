package com.prepaid.ledger.repository;

import com.prepaid.ledger.domain.LedgerLine;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LedgerLineRepository extends JpaRepository<LedgerLine, Long> {
}
