package com.prepaid.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 에러 응답 DTO
 * 클라이언트에게 일관된 형식으로 에러 정보 전달
 */
@Getter
@Builder
public class ErrorResponse {
    /**
     * 에러 코드 (예: INSUFFICIENT_BALANCE)
     */
    private String errorCode;

    /**
     * 에러 메시지
     */
    private String message;

    /**
     * 에러 발생 시각
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    /**
     * 요청 경로 (선택)
     */
    private String path;

    /**
     * 기본 생성자 (빌더용)
     */
    public ErrorResponse(String errorCode, String message, LocalDateTime timestamp, String path) {
        this.errorCode = errorCode;
        this.message = message;
        this.timestamp = timestamp != null ? timestamp : LocalDateTime.now();
        this.path = path;
    }

    /**
     * 간소화 생성자
     */
    public static ErrorResponse of(String errorCode, String message) {
        return ErrorResponse.builder()
                .errorCode(errorCode)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
