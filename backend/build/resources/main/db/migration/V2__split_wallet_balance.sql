-- Rename balance to balance_paid (Assuming existing balance is paid)
ALTER TABLE wallets RENAME COLUMN balance TO balance_paid;

-- Add balance_free
ALTER TABLE wallets ADD COLUMN balance_free BIGINT NOT NULL DEFAULT 0;
