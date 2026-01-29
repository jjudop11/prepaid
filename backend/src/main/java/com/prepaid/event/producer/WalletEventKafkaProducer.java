package com.prepaid.event.producer;

import com.prepaid.event.domain.WalletEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 트랜잭션이 커밋된 후 Kafka로 이벤트를 전송하는 프로듀서
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WalletEventKafkaProducer {

    private final KafkaTemplate<String, WalletEvent> kafkaTemplate;

    private static final String WALLET_EVENTS_TOPIC = "wallet-events";

    /**
     * 트랜잭션 커밋 완료 후 Kafka로 이벤트 전송
     * 
     * @param event 지갑 이벤트
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleWalletEvent(WalletEvent event) {
        log.info("트랜잭션 커밋 완료, Kafka 전송 시작: eventId={}, type={}, userId={}", 
                event.getEventId(), event.getEventType(), event.getUserId());
        
        try {
            String partitionKey = event.getUserId().toString();
            
            kafkaTemplate.send(WALLET_EVENTS_TOPIC, partitionKey, event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Kafka 이벤트 발행 성공: eventId={}, partition={}", 
                                event.getEventId(), result.getRecordMetadata().partition());
                    } else {
                        log.error("Kafka 이벤트 발행 실패: eventId={}", event.getEventId(), ex);
                        // 여기서 실패하면 재시도 로직이 필요할 수 있음 (Outbox 패턴 등 고려 가능)
                        // 현재는 로그만 남김
                    }
                });
        } catch (Exception e) {
            log.error("Kafka 전송 중 예외 발생: eventId={}", event.getEventId(), e);
        }
    }
}
