package com.prepaid.notification.listener;

import com.prepaid.event.domain.ChargeCompletedEvent;
import com.prepaid.event.domain.SpendCompletedEvent;
import com.prepaid.notification.service.SseEmitterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.stereotype.Component;

/**
 * SSE 알림 이벤트 리스너
 * - 충전/사용 이벤트 수신 시 SSE로 실시간 알림
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SseNotificationListener {
    
    private final SseEmitterService sseEmitterService;
    
    /**
     * 충전 완료 이벤트 처리
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleChargeCompleted(ChargeCompletedEvent event) {
        log.debug("충전 완료 이벤트 수신: userId={}", event.getUserId());
        
        // 잔액 업데이트 알림
        sseEmitterService.sendBalanceUpdate(event.getUserId(), event.getNewBalance());
        
        // 거래 완료 알림
        sseEmitterService.sendTransactionComplete(event.getUserId(), "CHARGE", event.getAmount());
    }
    
    /**
     * 사용 완료 이벤트 처리
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleSpendCompleted(SpendCompletedEvent event) {
        log.debug("사용 완료 이벤트 수신: userId={}", event.getUserId());
        
        // 잔액 업데이트 알림
        sseEmitterService.sendBalanceUpdate(event.getUserId(), event.getNewBalance());
        
        // 거래 완료 알림
        sseEmitterService.sendTransactionComplete(event.getUserId(), "USE", event.getAmount());
    }
}
