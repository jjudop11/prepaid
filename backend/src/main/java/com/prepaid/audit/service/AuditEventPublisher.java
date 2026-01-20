package com.prepaid.audit.service;

import com.prepaid.audit.event.AuditEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * 감사 이벤트 발행 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditEventPublisher {

    private final KafkaTemplate<String, AuditEvent> kafkaTemplate;
    
    private static final String AUDIT_EVENTS_TOPIC = "audit-events";

    /**
     * 감사 이벤트 발행
     */
    public void publish(AuditEvent event) {
        try {
            String partitionKey = event.getUserId().toString();
            
            kafkaTemplate.send(AUDIT_EVENTS_TOPIC, partitionKey, event)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            log.info("감사 이벤트 발행 성공: userId={}, action={}, result={}",
                                    event.getUserId(), event.getAction(), event.getResult());
                        } else {
                            log.error("감사 이벤트 발행 실패: userId={}, action={}",
                                    event.getUserId(), event.getAction(), ex);
                        }
                    });
        } catch (Exception e) {
            log.error("감사 이벤트 발행 중 예외: userId={}, action={}",
                    event.getUserId(), event.getAction(), e);
        }
    }
}
