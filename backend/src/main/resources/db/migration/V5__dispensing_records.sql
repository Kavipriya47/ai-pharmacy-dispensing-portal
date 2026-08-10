-- ============================================================
-- V5: Dispensation Records (ADCE Core)
-- ============================================================

CREATE TABLE dispensation_records (
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    medicine_id             BIGINT          NOT NULL,
    batch_id                BIGINT          NOT NULL,
    patient_identifier      VARCHAR(100)    NOT NULL,  -- anonymised patient ID, not a name
    prescription_reference  VARCHAR(200),              -- required when medicine.requires_prescription = TRUE
    quantity_dispensed      INT             NOT NULL,
    dispensed_by            VARCHAR(100)    NOT NULL,
    status                  VARCHAR(30)     NOT NULL DEFAULT 'COMPLETED',  -- DispensationStatus enum
    fefo_override           BOOLEAN         DEFAULT FALSE,  -- TRUE when pharmacist manually chose a batch
    override_reason         TEXT,
    notes                   TEXT,
    dispensed_at            TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_dispensation_medicine FOREIGN KEY (medicine_id) REFERENCES medicines(id),
    CONSTRAINT fk_dispensation_batch    FOREIGN KEY (batch_id)    REFERENCES medicine_batches(id),
    CONSTRAINT chk_dispensation_qty     CHECK (quantity_dispensed > 0)
);

CREATE INDEX idx_dispensation_medicine    ON dispensation_records (medicine_id);
CREATE INDEX idx_dispensation_batch       ON dispensation_records (batch_id);
CREATE INDEX idx_dispensation_patient     ON dispensation_records (patient_identifier);
CREATE INDEX idx_dispensation_dispensed_at ON dispensation_records (dispensed_at);
CREATE INDEX idx_dispensation_status      ON dispensation_records (status);
