package com.prepaid.ledger.scheduler;

import com.prepaid.ledger.domain.BucketType;
import com.prepaid.ledger.domain.ChargeLot;
import com.prepaid.ledger.repository.ChargeLotRepository;
import com.prepaid.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 포인트 만료 스케줄러
 * 매일 자정에 만료된 포인트 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PointExpirationScheduler {

    private final ChargeLotRepository chargeLotRepository;
    private final WalletRepository walletRepository;

    /**
     * 매일 자정 실행
     * 만료된 ChargeLot 처리
     */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void expirePoints() {
        // 1년 이상 된 충전 포인트 만료
        LocalDateTime expiryDate = LocalDateTime.now().minusYears(1);
        
        log.info("포인트 만료 처리 시작: expiryDate={}", expiryDate);
        
        try {
            // 만료 대상 조회
            List<ChargeLot> expiredLots = chargeLotRepository
                    .findAllByCreatedAtBeforeAndAmountRemainingGreaterThan(expiryDate, 0L);
            
            int expiredCount = 0;
            Long totalExpiredAmount = 0L;
            
            for (ChargeLot lot : expiredLots) {
                Long amountToExpire = lot.getAmountRemaining();
                
                // 포인트 만료 처리
                lot.expire();
                
                // 지갑 잔액 차감
                walletRepository.findById(lot.getWallet().getId()).ifPresent(wallet -> {
                    wallet.addBalance(-amountToExpire, lot.getBucketType());
                });
                
                totalExpiredAmount += amountToExpire;
                expiredCount++;
                
                log.debug("포인트 만료: lotId={}, amount={}", lot.getId(), amountToExpire);
            }
            
            log.info("포인트 만료 처리 완료: 만료 lot 수={}, 총 만료 금액={}원", 
                    expiredCount, totalExpiredAmount);
                    
        } catch (Exception e) {
            log.error("포인트 만료 처리 실패", e);
        }
    }
}
