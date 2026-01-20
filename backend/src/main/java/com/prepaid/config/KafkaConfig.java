package com.prepaid.config;

import com.prepaid.event.domain.WalletEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka 프로듀서 및 컨슈머 설정
 * - 재시도 로직: 1초 간격 3회
 * - DLQ (Dead Letter Queue) 지원
 */
@Slf4j
@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    /**
     * Kafka 프로듀서 설정
     * - JSON 직렬화
     * - 멱등성 활성화
     * - acks=all (모든 레플리카 확인)
     */
    @Bean
    public ProducerFactory<String, WalletEvent> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        // 멱등성 보장
        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3);
        configProps.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);

        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, WalletEvent> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    /**
     * AuditEvent용 프로듀서
     */
    @Bean
    public ProducerFactory<String, com.prepaid.audit.event.AuditEvent> auditEventProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3);

        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, com.prepaid.audit.event.AuditEvent> auditEventKafkaTemplate() {
        return new KafkaTemplate<>(auditEventProducerFactory());
    }

    /**
     * DLQ용 프로듀서 팩토리
     * - Object 타입으로 다양한 메시지 타입 지원
     */
    @Bean
    public ProducerFactory<String, Object> dlqProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, Object> dlqKafkaTemplate() {
        return new KafkaTemplate<>(dlqProducerFactory());
    }

    /**
     * Kafka 컨슈머 설정
     * - JSON 역직렬화
     * - 컨슈머 그룹: notification-service
     * - 자동 커밋 (ErrorHandler가 처리)
     */
    @Bean
    public ConsumerFactory<String, WalletEvent> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "notification-service");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);

        // JSON 역직렬화 설정
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.prepaid.event.domain");
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, WalletEvent.class);

        // 컨슈머 오프셋 관리
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);

        return new DefaultKafkaConsumerFactory<>(props);
    }

    /**
     * 에러 핸들러 설정
     * - 재시도: 1초 간격으로 3회
     * - 재시도 실패 시: DLQ (wallet-events.DLT) 토픽으로 전송
     */
    @Bean
    public CommonErrorHandler errorHandler(KafkaTemplate<String, Object> dlqKafkaTemplate) {
        // DLQ recoverer: 실패한 메시지를 DLT 토픽으로 전송
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(dlqKafkaTemplate,
                (record, exception) -> {
                    // DLQ 토픽 이름: 원본토픽.DLT
                    String dlqTopic = record.topic() + ".DLT";
                    log.error("메시지 DLQ 전송: topic={}, dlqTopic={}, key={}, exception={}",
                            record.topic(), dlqTopic, record.key(), exception.getMessage());
                    return new org.apache.kafka.common.TopicPartition(dlqTopic, record.partition());
                });

        // 재시도 정책: 1초 간격, 3회 재시도
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
                recoverer,
                new FixedBackOff(1000L, 3L) // 1초 간격, 3회 재시도
        );

        // 재시도 로깅
        errorHandler.setRetryListeners((record, ex, deliveryAttempt) ->
                log.warn("메시지 재시도 중: topic={}, partition={}, offset={}, attempt={}/{}, exception={}",
                        record.topic(), record.partition(), record.offset(),
                        deliveryAttempt, 3, ex.getMessage())
        );

        return errorHandler;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, WalletEvent> kafkaListenerContainerFactory(
            CommonErrorHandler errorHandler) {
        ConcurrentKafkaListenerContainerFactory<String, WalletEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());

        // ErrorHandler 설정 (재시도 + DLQ)
        factory.setCommonErrorHandler(errorHandler);

        // 자동 커밋 모드 (ErrorHandler가 관리)
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.BATCH);

        return factory;
    }
}
