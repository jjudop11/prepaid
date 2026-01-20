package com.prepaid.common.exception.specific;

import com.prepaid.common.exception.BusinessException;
import com.prepaid.common.exception.ErrorCode;

/**
 * 지갑을 찾을 수 없음 예외
 */
public class WalletNotFoundException extends BusinessException {
    public WalletNotFoundException() {
        super(ErrorCode.WALLET_NOT_FOUND);
    }

    public WalletNotFoundException(String customMessage) {
        super(ErrorCode.WALLET_NOT_FOUND, customMessage);
    }
}
