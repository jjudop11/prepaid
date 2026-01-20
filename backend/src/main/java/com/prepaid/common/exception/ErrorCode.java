package com.prepaid.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 에러 코드 정의
 * HTTP 상태 코드와 메시지 포함
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // 400 Bad Request
    INVALID_AMOUNT(HttpStatus.BAD_REQUEST, "유효하지 않은 금액입니다."),
    BELOW_MINIMUM_CHARGE(HttpStatus.BAD_REQUEST, "최소 충전 금액 미만입니다."),
    EXCEEDS_MAXIMUM_CHARGE(HttpStatus.BAD_REQUEST, "최대 충전 금액을 초과했습니다."),
    EXCEEDS_DAILY_LIMIT(HttpStatus.BAD_REQUEST, "일일 충전 한도를 초과했습니다."),
    INSUFFICIENT_BALANCE(HttpStatus.BAD_REQUEST, "잔액이 부족합니다."),
    INVALID_REFUND_AMOUNT(HttpStatus.BAD_REQUEST, "환불 금액이 유효하지 않습니다."),
    REFUND_PERIOD_EXPIRED(HttpStatus.BAD_REQUEST, "환불 가능 기간이 지났습니다."),

    // 404 Not Found
    WALLET_NOT_FOUND(HttpStatus.NOT_FOUND, "지갑을 찾을 수 없습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    TRANSACTION_NOT_FOUND(HttpStatus.NOT_FOUND, "거래 내역을 찾을 수 없습니다."),
    LEDGER_ENTRY_NOT_FOUND(HttpStatus.NOT_FOUND, "원장 기록을 찾을 수 없습니다."),

    // 409 Conflict
    DUPLICATE_REQUEST(HttpStatus.CONFLICT, "이미 처리 중이거나 처리된 요청입니다."),
    WALLET_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 지갑이 존재합니다."),

    // 500 Internal Server Error
    PAYMENT_GATEWAY_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "결제 시스템 오류가 발생했습니다."),
    LEDGER_INCONSISTENCY(HttpStatus.INTERNAL_SERVER_ERROR, "원장 불일치 오류가 발생했습니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "내부 서버 오류가 발생했습니다."),

    // 503 Service Unavailable
    EXTERNAL_SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "외부 서비스를 사용할 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
