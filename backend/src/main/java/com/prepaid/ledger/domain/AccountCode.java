package com.prepaid.ledger.domain;

public enum AccountCode {
    WALLET_CASH, // User's cash (Liability for platform)
    EXTERNAL_CASH_IN, // Physical money received (Asset)
    REWARD_EXPENSE // Marketing expense
}
