package com.prepaid.common.exception.specific;

import com.prepaid.common.exception.BusinessException;
import com.prepaid.common.exception.ErrorCode;

/**
 * 유효하지 않은 금액 예외
 */
public class InvalidAmountException extends BusinessException {
    public InvalidAmountException() {
        super(ErrorCode.INVALID_AMOUNT);
    }

    public InvalidAmountException(ErrorCode errorCode) {
        super(errorCode);
    }

    public InvalidAmountException(ErrorCode errorCode, String customMessage) {
        super(errorCode, customMessage);
    }
}
