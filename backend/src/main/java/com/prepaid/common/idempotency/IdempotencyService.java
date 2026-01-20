package com.prepaid.common.idempotency;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prepaid.common.exception.specific.DuplicateRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * 멱등성 서비스
 * Redis를 사용하여 중복 요청 방지
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String IDEMPOTENCY_PREFIX = "idempotency:";
    private static final Duration TTL = Duration.ofHours(24); // 24시간 보관

    /**
     * 요청 처리 시작 (중복 체크)
     * 
     * @param idempotencyKey 멱등성 키
     * @throws DuplicateRequestException 이미 처리 중이거나 완료된 요청
     */
    public void startProcessing(String idempotencyKey) {
        String key = getKey(idempotencyKey);

        // 기존 요청 확인
        String existingValue = redisTemplate.opsForValue().get(key);
        
        if (existingValue != null) {
            try {
                IdempotentRequest existing = objectMapper.readValue(existingValue, IdempotentRequest.class);
                
                if ("PROCESSING".equals(existing.getStatus())) {
                    log.warn("중복 요청 감지 (처리 중): {}", idempotencyKey);
                    throw new DuplicateRequestException("요청이 이미 처리 중입니다.");
                }
                
                if ("COMPLETED".equals(existing.getStatus())) {
                    log.warn("중복 요청 감지 (완료됨): {}", idempotencyKey);
                    throw new DuplicateRequestException("이미 처리된 요청입니다.");
                }
                
            } catch (Exception e) {
                if (e instanceof DuplicateRequestException) {
                    throw (DuplicateRequestException) e;
                }
                log.error("멱등성 키 파싱 오류: {}", idempotencyKey, e);
            }
        }

        // PROCESSING 상태로 저장
        try {
            IdempotentRequest processing = IdempotentRequest.processing(idempotencyKey);
            String value = objectMapper.writeValueAsString(processing);
            redisTemplate.opsForValue().set(key, value, TTL.toMillis(), TimeUnit.MILLISECONDS);
            log.info("멱등성 키 등록 (PROCESSING): {}", idempotencyKey);
        } catch (Exception e) {
            log.error("멱등성 키 저장 실패: {}", idempotencyKey, e);
        }
    }

    /**
     * 요청 처리 완료
     * 
     * @param idempotencyKey 멱등성 키
     * @param result 처리 결과
     */
    public void markCompleted(String idempotencyKey, Object result) {
        String key = getKey(idempotencyKey);
        
        try {
            IdempotentRequest completed = IdempotentRequest.completed(idempotencyKey, result);
            String value = objectMapper.writeValueAsString(completed);
            redisTemplate.opsForValue().set(key, value, TTL.toMillis(), TimeUnit.MILLISECONDS);
            log.info("멱등성 키 완료 처리: {}", idempotencyKey);
        } catch (Exception e) {
            log.error("멱등성 키 완료 처리 실패: {}", idempotencyKey, e);
        }
    }

    /**
     * 요청 처리 실패
     * 
     * @param idempotencyKey 멱등성 키
     * @param errorMessage 에러 메시지
     */
    public void markFailed(String idempotencyKey, String errorMessage) {
        String key = getKey(idempotencyKey);
        
        try {
            IdempotentRequest failed = IdempotentRequest.failed(idempotencyKey, errorMessage);
            String value = objectMapper.writeValueAsString(failed);
            // 실패한 요청은 짧은 시간만 보관 (재시도 가능)
            redisTemplate.opsForValue().set(key, value, Duration.ofMinutes(10).toMillis(), TimeUnit.MILLISECONDS);
            log.info("멱등성 키 실패 처리: {}", idempotencyKey);
        } catch (Exception e) {
            log.error("멱등성 키 실패 처리 실패: {}", idempotencyKey, e);
        }
    }

    /**
     * 멱등성 키 생성
     */
    private String getKey(String idempotencyKey) {
        return IDEMPOTENCY_PREFIX + idempotencyKey;
    }

    /**
     * 멱등성 키 삭제 (테스트용)
     */
    public void delete(String idempotencyKey) {
        String key = getKey(idempotencyKey);
        redisTemplate.delete(key);
        log.info("멱등성 키 삭제: {}", idempotencyKey);
    }
}
