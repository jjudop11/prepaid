package com.prepaid.ledger.repository;

import com.prepaid.ledger.domain.ChargeLot;
import com.prepaid.ledger.domain.BucketType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChargeLotRepository extends JpaRepository<ChargeLot, Long> {
    List<ChargeLot> findAllByWalletIdAndBucketTypeAndAmountRemainingGreaterThanOrderByCreatedAtAsc(Long walletId,
            BucketType bucketType, Long amountRemaining);
}
