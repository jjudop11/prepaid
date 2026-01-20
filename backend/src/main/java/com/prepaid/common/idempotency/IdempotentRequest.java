package com.prepaid.common.idempotency;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 멱등성 요청 상태 저장 객체
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IdempotentRequest {
    /**
     * 멱등성 키
     */
    private String idempotencyKey;

    /**
     * 처리 상태: PROCESSING, COMPLETED, FAILED
     */
    private String status;

    /**
     * 처리 결과 (성공 시)
     */
    private Object result;

    /**
     * 에러 메시지 (실패 시)
     */
    private String errorMessage;

    /**
     * 생성 시간
     */
    private long timestamp;

    public static IdempotentRequest processing(String idempotencyKey) {
        return new IdempotentRequest(idempotencyKey, "PROCESSING", null, null, System.currentTimeMillis());
    }

    public static IdempotentRequest completed(String idempotencyKey, Object result) {
        return new IdempotentRequest(idempotencyKey, "COMPLETED", result, null, System.currentTimeMillis());
    }

    public static IdempotentRequest failed(String idempotencyKey, String errorMessage) {
        return new IdempotentRequest(idempotencyKey, "FAILED", null, errorMessage, System.currentTimeMillis());
    }
}
