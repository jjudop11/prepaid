package com.prepaid.common.exception.specific;

import com.prepaid.common.exception.BusinessException;
import com.prepaid.common.exception.ErrorCode;

/**
 * 사용자를 찾을 수 없음 예외
 */
public class UserNotFoundException extends BusinessException {
    public UserNotFoundException() {
        super(ErrorCode.USER_NOT_FOUND);
    }

    public UserNotFoundException(String customMessage) {
        super(ErrorCode.USER_NOT_FOUND, customMessage);
    }
}
