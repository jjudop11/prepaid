package com.prepaid.event.service;

import com.prepaid.event.domain.WalletEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * Kafka로 이벤트를 발행하는 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    /**
     * 지갑 이벤트 발행
     * userId를 파티션 키로 사용하여 사용자별 순서 보장
     * 
     * @param event 발행할 이벤트
     */
    public void publish(WalletEvent event) {
        log.info("이벤트 발행 (Internal): eventId={}, type={}, userId={}", 
                event.getEventId(), event.getEventType(), event.getUserId());
        
        // Spring Application Event 발행 (트랜잭션 내부)
        applicationEventPublisher.publishEvent(event);
    }
}
