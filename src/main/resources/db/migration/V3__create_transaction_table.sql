CREATE TABLE transactions (
    id UUID PRIMARY KEY,
    idempotency_key VARCHAR(100) NOT NULL UNIQUE,
    source_wallet_id UUID REFERENCES wallets(id) ON DELETE RESTRICT,
    destination_wallet_id UUID REFERENCES wallets(id) ON DELETE RESTRICT,
    amount NUMERIC(19, 4) NOT NULL,
    transaction_type VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,
    description VARCHAR(255),
    error_reason VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP WITH TIME ZONE,
    
    CONSTRAINT chk_transaction_amount_positive CHECK (amount > 0)
);

CREATE INDEX idx_transactions_source_wallet ON transactions(source_wallet_id, created_at DESC);
CREATE INDEX idx_transactions_destination_wallet ON transactions(destination_wallet_id, created_at DESC);
CREATE INDEX idx_transactions_status ON transactions(status);
