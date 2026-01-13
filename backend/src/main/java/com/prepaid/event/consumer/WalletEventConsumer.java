package com.prepaid.event.consumer;

import com.prepaid.event.domain.ChargeCompletedEvent;
import com.prepaid.event.domain.ReversalCompletedEvent;
import com.prepaid.event.domain.SpendCompletedEvent;
import com.prepaid.event.domain.WalletEvent;
import com.prepaid.notification.dto.NotificationDto;
import com.prepaid.notification.service.SseConnectionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Kafka 이벤트를 소비하고 SSE로 알림을 전송하는 컨슈머
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WalletEventConsumer {

    private final SseConnectionManager sseConnectionManager;

    /**
     * wallet-events 토픽에서 이벤트 소비
     * 
     * @param event          지갑 이벤트
     * @param acknowledgment 수동 커밋용
     */
    @KafkaListener(topics = "wallet-events", groupId = "notification-service", containerFactory = "kafkaListenerContainerFactory")
    public void consume(WalletEvent event, Acknowledgment acknowledgment) {
        try {
            log.info("이벤트 수신: eventId={}, type={}, userId={}",
                    event.getEventId(), event.getEventType(), event.getUserId());

            // 이벤트를 알림 DTO로 변환
            NotificationDto notification = convertToNotification(event);

            // SSE를 통해 사용자에게 전송
            sseConnectionManager.sendToUser(event.getUserId(), notification);

            // 수동 커밋
            acknowledgment.acknowledge();

            log.info("이벤트 처리 완료: eventId={}, type={}", event.getEventId(), event.getEventType());
        } catch (Exception e) {
            log.error("이벤트 처리 실패: eventId={}, type={}", event.getEventId(), event.getEventType(), e);
            // 에러 발생 시에도 커밋 (DLQ 전송은 향후 구현)
            acknowledgment.acknowledge();
        }
    }

    /**
     * 이벤트를 알림 DTO로 변환
     * 
     * @param event 지갑 이벤트
     * @return 알림 DTO
     */
    private NotificationDto convertToNotification(WalletEvent event) {
        return switch (event.getEventType()) {
            case CHARGE_COMPLETED -> convertChargeEvent((ChargeCompletedEvent) event);
            case SPEND_COMPLETED -> convertSpendEvent((SpendCompletedEvent) event);
            case REVERSAL_COMPLETED -> convertReversalEvent((ReversalCompletedEvent) event);
        };
    }

    /**
     * 충전 완료 이벤트를 알림으로 변환
     */
    private NotificationDto convertChargeEvent(ChargeCompletedEvent event) {
        String formattedAmount = formatCurrency(event.getAmount());
        String formattedBalance = formatCurrency(event.getNewBalance());

        return NotificationDto.builder()
                .eventId(event.getEventId())
                .eventType(event.getEventType())
                .title("충전 완료")
                .message(String.format("%s가 충전되었습니다. 현재 잔액: %s", formattedAmount, formattedBalance))
                .occurredAt(event.getOccurredAt())
                .notificationType("success")
                .build();
    }

    /**
     * 사용 완료 이벤트를 알림으로 변환
     */
    private NotificationDto convertSpendEvent(SpendCompletedEvent event) {
        String formattedAmount = formatCurrency(Math.abs(event.getAmount()));
        String formattedBalance = formatCurrency(event.getNewBalance());

        String message = event.getDescription() != null
                ? String.format("%s - %s 사용 완료. 현재 잔액: %s", event.getDescription(), formattedAmount, formattedBalance)
                : String.format("%s 사용 완료. 현재 잔액: %s", formattedAmount, formattedBalance);

        return NotificationDto.builder()
                .eventId(event.getEventId())
                .eventType(event.getEventType())
                .title("사용 완료")
                .message(message)
                .occurredAt(event.getOccurredAt())
                .notificationType("info")
                .build();
    }

    /**
     * 취소 완료 이벤트를 알림으로 변환
     */
    private NotificationDto convertReversalEvent(ReversalCompletedEvent event) {
        String formattedAmount = formatCurrency(event.getReversedAmount());
        String formattedBalance = formatCurrency(event.getNewBalance());

        String message = event.getReason() != null
                ? String.format("취소 완료 (%s): %s 반환. 현재 잔액: %s", event.getReason(), formattedAmount, formattedBalance)
                : String.format("취소 완료: %s 반환. 현재 잔액: %s", formattedAmount, formattedBalance);

        return NotificationDto.builder()
                .eventId(event.getEventId())
                .eventType(event.getEventType())
                .title("취소 완료")
                .message(message)
                .occurredAt(event.getOccurredAt())
                .notificationType("info")
                .build();
    }

    /**
     * 금액을 한국 원화 형식으로 포맷
     */
    private String formatCurrency(Long amount) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.KOREA);
        return formatter.format(amount);
    }
}
