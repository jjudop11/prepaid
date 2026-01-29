package com.prepaid.ledger.domain;

public enum TxType {
    CHARGE,         // 충전 (Toss 결제 승인)
    CHARGE_CANCEL,  // 충전 취소 (특정 ChargeLot 무효화, Toss 결제 취소)
    USE,            // 사용 (가맹점 결제)
    REVERSAL,       // 사용 취소 (가맹점 환불, ChargeLot 원복)
    REFUND,         // 환불/인출 (은행 계좌로 출금)
    REWARD          // 보상/적립 (프로모션 등)
}
