package com.prepaid.common.exception.specific;

import com.prepaid.common.exception.BusinessException;
import com.prepaid.common.exception.ErrorCode;

/**
 * 중복 요청 예외 (멱등성 위반)
 */
public class DuplicateRequestException extends BusinessException {
    public DuplicateRequestException() {
        super(ErrorCode.DUPLICATE_REQUEST);
    }

    public DuplicateRequestException(String customMessage) {
        super(ErrorCode.DUPLICATE_REQUEST, customMessage);
    }
}
