-- Users
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    provider VARCHAR(50) NOT NULL, -- 'NAVER', etc.
    email VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_users_provider_email UNIQUE (provider, email)
);

-- Wallets
CREATE TABLE wallets (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    balance BIGINT NOT NULL DEFAULT 0, -- Derived/Cache
    version BIGINT NOT NULL DEFAULT 0, -- Optimistic Locking
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, DEBT, SUSPENDED
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_wallets_user FOREIGN KEY (user_id) REFERENCES users(id)
);
-- Note: No check constraint on balance >= 0 because DEBT is allowed.

-- Ledger Entries (Transaction Header)
CREATE TABLE ledger_entries (
    id BIGSERIAL PRIMARY KEY,
    wallet_id BIGINT NOT NULL,
    tx_type_code VARCHAR(10) NOT NULL, -- 00=CHARGE, 01=USE, 02=REWARD, 03=REVERSAL
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- PENDING, POSTED, FAILED
    reference_id VARCHAR(100), -- Order ID, Payment Key
    idempotency_key VARCHAR(100) UNIQUE NOT NULL,
    bucket_type VARCHAR(20), -- PAID, FREE (Nullable, mostly for CHARGE/REWARD)
    origin_entry_id BIGINT, -- Link REWARD to CHARGE
    reversed_entry_id BIGINT, -- Link REVERSAL to Original Entry
    memo VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ledger_wallet FOREIGN KEY (wallet_id) REFERENCES wallets(id)
);
CREATE INDEX idx_ledger_wallet_created ON ledger_entries(wallet_id, created_at);
CREATE INDEX idx_ledger_reference ON ledger_entries(reference_id);

-- Ledger Lines (Double Entry Lines)
CREATE TABLE ledger_lines (
    id BIGSERIAL PRIMARY KEY,
    entry_id BIGINT NOT NULL,
    account_code VARCHAR(50) NOT NULL, -- WALLET_CASH, REWARD_EXPENSE, etc.
    amount_signed BIGINT NOT NULL, -- Positive or Negative
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_lines_entry FOREIGN KEY (entry_id) REFERENCES ledger_entries(id),
    CONSTRAINT chk_amount_nonzero CHECK (amount_signed != 0)
);
CREATE INDEX idx_lines_entry ON ledger_lines(entry_id);

-- Charge Lots (Operational Projection for FIFO)
CREATE TABLE charge_lots (
    id BIGSERIAL PRIMARY KEY,
    wallet_id BIGINT NOT NULL,
    bucket_type VARCHAR(20) NOT NULL, -- PAID, FREE
    original_entry_id BIGINT NOT NULL UNIQUE, -- The CHARGE entry creating this lot
    amount_total BIGINT NOT NULL, -- Initial amount
    amount_remaining BIGINT NOT NULL, -- Current remaining
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_lots_wallet FOREIGN KEY (wallet_id) REFERENCES wallets(id),
    CONSTRAINT chk_lot_amounts CHECK (amount_total > 0 AND amount_remaining >= 0)
);

-- Spend Allocations (Linking Spend to Lots)
CREATE TABLE spend_allocations (
    id BIGSERIAL PRIMARY KEY,
    spend_entry_id BIGINT NOT NULL, -- The USE entry
    charge_lot_id BIGINT NOT NULL, -- The Lot being consumed
    amount_consumed BIGINT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_alloc_spend FOREIGN KEY (spend_entry_id) REFERENCES ledger_entries(id),
    CONSTRAINT fk_alloc_lot FOREIGN KEY (charge_lot_id) REFERENCES charge_lots(id),
    CONSTRAINT chk_alloc_amount CHECK (amount_consumed > 0)
);
CREATE INDEX idx_alloc_spend ON spend_allocations(spend_entry_id);
CREATE INDEX idx_alloc_lot ON spend_allocations(charge_lot_id);
