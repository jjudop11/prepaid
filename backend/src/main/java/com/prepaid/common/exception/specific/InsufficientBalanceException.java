package com.prepaid.common.exception.specific;

import com.prepaid.common.exception.BusinessException;
import com.prepaid.common.exception.ErrorCode;

/**
 * 잔액 부족 예외
 */
public class InsufficientBalanceException extends BusinessException {
    public InsufficientBalanceException() {
        super(ErrorCode.INSUFFICIENT_BALANCE);
    }

    public InsufficientBalanceException(String customMessage) {
        super(ErrorCode.INSUFFICIENT_BALANCE, customMessage);
    }
}
