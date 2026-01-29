package com.prepaid.audit.consumer;

import com.prepaid.audit.domain.AuditLog;
import com.prepaid.audit.event.AuditEvent;
import com.prepaid.audit.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

/**
 * 감사 이벤트 Consumer
 * - PostgreSQL에 저장
 * - Elasticsearch 전송 (향후 구현)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditEventConsumer {

    private final AuditLogRepository auditLogRepository;
    // private final ElasticsearchClient elasticsearchClient; // 향후 추가

    @KafkaListener(topics = "audit-events", groupId = "audit-service")
    public void consume(
        @Payload AuditEvent event,
        @Header(value = "traceId", required = false) String traceId,
        @Header(value = "spanId", required = false) String spanId
    ) {
        // Kafka 헤더에서 Trace ID 복원
        if (traceId != null && !traceId.isEmpty()) {
            MDC.put("traceId", traceId);
        }
        if (spanId != null && !spanId.isEmpty()) {
            MDC.put("spanId", spanId);
        }
        
        try {
            log.info("감사 이벤트 수신");

            // 1. PostgreSQL 저장
            AuditLog auditLog = AuditLog.builder()
                    .userId(event.getUserId())
                    .action(event.getAction())
                    .amount(event.getAmount())
                    .ipAddress(event.getIpAddress())
                    .userAgent(event.getUserAgent())
                    .result(event.getResult())
                    .errorMessage(event.getErrorMessage())
                    .referenceId(event.getReferenceId())
                    .timestamp(event.getTimestamp())
                    .build();
            
            auditLogRepository.save(auditLog);
            log.info("감사 로그 저장 완료: id={}", auditLog.getId());

            // 2. Elasticsearch 전송 (향후 구현)
            // if (elasticsearchEnabled) {
            //     elasticsearchClient.index(toElasticsearchDoc(event));
            // }
            
        } catch (Exception e) {
            log.error("감사 로그 저장 실패: userId={}, action={}",
                    event.getUserId(), event.getAction(), e);
        } finally {
            // MDC 정리
            MDC.clear();
        }
    }
}
