package com.prepaid.ledger.domain;

public enum TxType {
    CHARGE,  // 충전
    USE,     // 사용
    REFUND,  // 환불
    REWARD, // 02
    REVERSAL // 03
}
