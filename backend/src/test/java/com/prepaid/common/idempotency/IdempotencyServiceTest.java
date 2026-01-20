package com.prepaid.common.idempotency;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prepaid.common.exception.specific.DuplicateRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * IdempotencyService 단위 테스트
 */
@DisplayName("IdempotencyService 테스트")
class IdempotencyServiceTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private IdempotencyService idempotencyService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();
        idempotencyService = new IdempotencyService(redisTemplate, objectMapper);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("처음 요청은 정상 처리")
    void startProcessing_FirstRequest_Success() {
        // given
        String idempotencyKey = "test-key-1";
        when(valueOperations.get(anyString())).thenReturn(null);

        // when & then
        assertDoesNotThrow(() -> idempotencyService.startProcessing(idempotencyKey));
        verify(valueOperations).set(anyString(), anyString(), anyLong(), eq(TimeUnit.MILLISECONDS));
    }

    @Test
    @DisplayName("처리 중인 요청 중복 차단")
    void startProcessing_ProcessingRequest_ThrowsDuplicate() throws Exception {
        // given
        String idempotencyKey = "test-key-2";
        IdempotentRequest processing = IdempotentRequest.processing(idempotencyKey);
        when(valueOperations.get(anyString())).thenReturn(objectMapper.writeValueAsString(processing));

        // when & then
        assertThatThrownBy(() -> idempotencyService.startProcessing(idempotencyKey))
                .isInstanceOf(DuplicateRequestException.class)
                .hasMessageContaining("처리 중");
    }

    @Test
    @DisplayName("완료된 요청 중복 차단")
    void startProcessing_CompletedRequest_ThrowsDuplicate() throws Exception {
        // given
        String idempotencyKey = "test-key-3";
        IdempotentRequest completed = IdempotentRequest.completed(idempotencyKey, "SUCCESS");
        when(valueOperations.get(anyString())).thenReturn(objectMapper.writeValueAsString(completed));

        // when & then
        assertThatThrownBy(() -> idempotencyService.startProcessing(idempotencyKey))
                .isInstanceOf(DuplicateRequestException.class)
                .hasMessageContaining("처리된 요청");
    }

    @Test
    @DisplayName("처리 완료 표시")
    void markCompleted_Success() {
        // given
        String idempotencyKey = "test-key-4";

        // when
        idempotencyService.markCompleted(idempotencyKey, "SUCCESS");

        // then
        verify(valueOperations).set(anyString(), anyString(), anyLong(), eq(TimeUnit.MILLISECONDS));
    }
}
