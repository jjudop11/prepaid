package com.prepaid.common.exception.specific;

import com.prepaid.common.exception.BusinessException;
import com.prepaid.common.exception.ErrorCode;

/**
 * 권한 없음 예외
 */
public class UnauthorizedException extends BusinessException {
    public UnauthorizedException() {
        super(ErrorCode.UNAUTHORIZED);
    }
    
    public UnauthorizedException(String customMessage) {
        super(ErrorCode.UNAUTHORIZED, customMessage);
    }
}
