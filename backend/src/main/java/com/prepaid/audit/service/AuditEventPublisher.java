package com.prepaid.audit.service;

import com.prepaid.audit.event.AuditEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
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
     * 감사 이벤트 발행 (Trace ID 포함)
     */
    public void publish(AuditEvent event) {
        try {
            String partitionKey = event.getUserId().toString();
            
            // MDC에서 Trace ID 가져오기
            String traceId = MDC.get("traceId");
            String spanId = MDC.get("spanId");
            
            // Kafka 메시지에 Trace ID 포함
            Message<AuditEvent> message = MessageBuilder
                .withPayload(event)
                .setHeader(KafkaHeaders.TOPIC, AUDIT_EVENTS_TOPIC)
                .setHeader(KafkaHeaders.KEY, partitionKey)
                .setHeader("traceId", traceId != null ? traceId : "")
                .setHeader("spanId", spanId != null ? spanId : "")
                .build();
            
            kafkaTemplate.send(message)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            log.info("감사 이벤트 발행 성공: traceId={}", traceId);
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
