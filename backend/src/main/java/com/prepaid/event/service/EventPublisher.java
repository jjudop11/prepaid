package com.prepaid.event.service;

import com.prepaid.event.domain.WalletEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * Kafka로 이벤트를 발행하는 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EventPublisher {

    private final KafkaTemplate<String, WalletEvent> kafkaTemplate;

    /**
     * 토픽 이름
     */
    private static final String WALLET_EVENTS_TOPIC = "wallet-events";

    /**
     * 지갑 이벤트 발행
     * userId를 파티션 키로 사용하여 사용자별 순서 보장
     * 
     * @param event 발행할 이벤트
     */
    public void publish(WalletEvent event) {
        try {
            // userId를 파티션 키로 사용하여 동일 사용자의 이벤트는 순서 보장
            String partitionKey = event.getUserId().toString();

            kafkaTemplate.send(WALLET_EVENTS_TOPIC, partitionKey, event)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            log.info("이벤트 발행 성공: eventId={}, type={}, userId={}, partition={}",
                                    event.getEventId(),
                                    event.getEventType(),
                                    event.getUserId(),
                                    result.getRecordMetadata().partition());
                        } else {
                            log.error("이벤트 발행 실패: eventId={}, type={}, userId={}",
                                    event.getEventId(),
                                    event.getEventType(),
                                    event.getUserId(),
                                    ex);
                        }
                    });
        } catch (Exception e) {
            // 이벤트 발행 실패는 비즈니스 트랜잭션을 실패시키지 않음
            log.error("이벤트 발행 중 예외 발생: eventId={}, type={}",
                    event.getEventId(),
                    event.getEventType(),
                    e);
        }
    }
}
