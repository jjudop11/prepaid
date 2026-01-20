package com.prepaid.event.consumer;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * DLQ (Dead Letter Queue) 컨슈머
 * - 재시도 실패한 메시지 모니터링 및 로깅
 * - 향후 알림 시스템 또는 수동 재처리 로직 추가 가능
 */
@Slf4j
@Service
public class DlqConsumer {

    /**
     * DLQ 토픽에서 실패한 메시지 소비
     * - 에러 로깅
     * - 모니터링 메트릭 수집 (향후 구현)
     * - 알림 발송 (향후 구현)
     *
     * @param record 실패한 원본 레코드
     */
    @KafkaListener(topics = "wallet-events.DLT", groupId = "dlq-monitor")
    public void consumeDeadLetter(ConsumerRecord<String, Object> record) {
        log.error("========================================");
        log.error("DLQ 메시지 수신 - 재시도 3회 실패");
        log.error("Topic: {}", record.topic());
        log.error("Partition: {}", record.partition());
        log.error("Offset: {}", record.offset());
        log.error("Key: {}", record.key());
        log.error("Value: {}", record.value());
        log.error("Timestamp: {}", record.timestamp());
        
        // 헤더에서 원본 토픽과 예외 정보 추출
        record.headers().forEach(header -> {
            String headerKey = header.key();
            if (headerKey.startsWith("kafka_dlt-") || headerKey.contains("exception")) {
                log.error("Header [{}]: {}", headerKey, new String(header.value()));
            }
        });
        
        log.error("========================================");
        
        // TODO: 향후 구현 고려사항
        // - Slack/Email 알림 발송
        // - 모니터링 시스템 (Prometheus, Grafana) 메트릭 수집
        // - 데이터베이스에 실패 이력 저장
        // - 관리자 대시보드에 실시간 표시
        // - 수동 재처리 API 제공
    }
}
