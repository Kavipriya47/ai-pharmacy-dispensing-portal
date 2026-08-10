-- ============================================================
-- V4: Inventory, Medicine Batches & Inventory Transactions
-- ============================================================

-- -------------------------------------------------------
-- inventory: one row per medicine, tracks aggregate stock
-- -------------------------------------------------------
CREATE TABLE inventory (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    medicine_id         BIGINT         NOT NULL UNIQUE,
    total_quantity      INT            NOT NULL DEFAULT 0,
    version             BIGINT         NOT NULL DEFAULT 0,  -- optimistic locking
    created_at          TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_inventory_medicine FOREIGN KEY (medicine_id) REFERENCES medicines(id)
);

CREATE INDEX idx_inventory_medicine ON inventory (medicine_id);

-- -------------------------------------------------------
-- medicine_batches: one row per procurement / lot
-- -------------------------------------------------------
CREATE TABLE medicine_batches (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    inventory_id        BIGINT         NOT NULL,
    batch_number        VARCHAR(100)   NOT NULL,
    manufacturer        VARCHAR(200),
    manufacturing_date  DATE,
    expiry_date         DATE           NOT NULL,
    quantity_received   INT            NOT NULL,
    quantity_remaining  INT            NOT NULL,
    unit_cost           DECIMAL(10,2),
    status              VARCHAR(30)    NOT NULL DEFAULT 'ACTIVE',  -- BatchStatus enum
    version             BIGINT         NOT NULL DEFAULT 0,          -- optimistic locking
    created_at          TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_batch_inventory FOREIGN KEY (inventory_id) REFERENCES inventory(id),
    CONSTRAINT chk_batch_quantity CHECK (quantity_remaining >= 0),
    CONSTRAINT chk_received_positive CHECK (quantity_received > 0)
);

CREATE INDEX idx_batch_inventory       ON medicine_batches (inventory_id);
CREATE INDEX idx_batch_status          ON medicine_batches (status);
CREATE INDEX idx_batch_expiry          ON medicine_batches (expiry_date);
CREATE INDEX idx_batch_number          ON medicine_batches (batch_number);
-- FEFO query support: active batches by expiry ASC
CREATE INDEX idx_batch_fefo            ON medicine_batches (inventory_id, status, expiry_date);

-- -------------------------------------------------------
-- inventory_transactions: immutable ledger (insert-only)
-- -------------------------------------------------------
CREATE TABLE inventory_transactions (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    batch_id            BIGINT         NOT NULL,
    transaction_type    VARCHAR(30)    NOT NULL,  -- TransactionType enum
    quantity            INT            NOT NULL,
    performed_by        VARCHAR(100)   NOT NULL,
    notes               TEXT,
    reference_id        VARCHAR(200),             -- dispensation_id / purchase_order / etc.
    created_at          TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_txn_batch FOREIGN KEY (batch_id) REFERENCES medicine_batches(id),
    CONSTRAINT chk_txn_quantity CHECK (quantity > 0)
);

CREATE INDEX idx_txn_batch   ON inventory_transactions (batch_id);
CREATE INDEX idx_txn_type    ON inventory_transactions (transaction_type);
CREATE INDEX idx_txn_user    ON inventory_transactions (performed_by);
CREATE INDEX idx_txn_created ON inventory_transactions (created_at);

-- -------------------------------------------------------
-- Audit tables (Hibernate Envers)
-- -------------------------------------------------------
CREATE TABLE inventory_aud (
    id                  BIGINT NOT NULL,
    rev                 INT NOT NULL,
    revtype             TINYINT,
    medicine_id         BIGINT,
    total_quantity      INT,
    version             BIGINT,
    created_at          TIMESTAMP,
    updated_at          TIMESTAMP,
    PRIMARY KEY (id, rev),
    CONSTRAINT fk_inventory_aud_rev FOREIGN KEY (rev) REFERENCES revinfo(rev)
);

CREATE TABLE medicine_batches_aud (
    id                  BIGINT NOT NULL,
    rev                 INT NOT NULL,
    revtype             TINYINT,
    inventory_id        BIGINT,
    batch_number        VARCHAR(100),
    manufacturer        VARCHAR(200),
    manufacturing_date  DATE,
    expiry_date         DATE,
    quantity_received   INT,
    quantity_remaining  INT,
    unit_cost           DECIMAL(10,2),
    status              VARCHAR(30),
    version             BIGINT,
    created_at          TIMESTAMP,
    updated_at          TIMESTAMP,
    PRIMARY KEY (id, rev),
    CONSTRAINT fk_batch_aud_rev FOREIGN KEY (rev) REFERENCES revinfo(rev)
);
