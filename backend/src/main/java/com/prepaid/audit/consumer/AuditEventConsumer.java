package com.prepaid.audit.consumer;

import com.prepaid.audit.domain.AuditLog;
import com.prepaid.audit.event.AuditEvent;
import com.prepaid.audit.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
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
    public void consume(AuditEvent event) {
        log.info("감사 이벤트 수신: userId={}, action={}, result={}",
                event.getUserId(), event.getAction(), event.getResult());

        try {
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
        }
    }
}
