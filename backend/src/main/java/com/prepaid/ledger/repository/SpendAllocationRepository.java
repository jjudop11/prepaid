package com.prepaid.ledger.repository;

import com.prepaid.ledger.domain.SpendAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface SpendAllocationRepository extends JpaRepository<SpendAllocation, Long> {
    @Query("SELECT sa FROM SpendAllocation sa JOIN FETCH sa.chargeLot")
    List<SpendAllocation> findAllWithChargeLot();
}
