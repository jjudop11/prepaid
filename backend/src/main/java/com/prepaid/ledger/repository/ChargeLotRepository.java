package com.prepaid.ledger.repository;

import com.prepaid.ledger.domain.ChargeLot;
import com.prepaid.ledger.domain.BucketType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ChargeLotRepository extends JpaRepository<ChargeLot, Long> {
    List<ChargeLot> findAllByWalletIdAndBucketTypeAndAmountRemainingGreaterThanOrderByCreatedAtAsc(Long walletId,
            BucketType bucketType, Long amountRemaining);

    /**
     * 만료 대상 조회 (생성일 기준)
     */
    List<ChargeLot> findAllByCreatedAtBeforeAndAmountRemainingGreaterThan(
            LocalDateTime expiryDate, Long amountRemaining);
}
