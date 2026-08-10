-- ============================================================
-- V3: Medicine Catalog (Suppliers + Medicines)
-- ============================================================

-- Suppliers Table
CREATE TABLE suppliers (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(150)  NOT NULL,
    contact_person VARCHAR(100),
    email       VARCHAR(100)  UNIQUE,
    phone       VARCHAR(30),
    address     TEXT,
    active      BOOLEAN       DEFAULT TRUE,
    created_at  TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE INDEX idx_supplier_name ON suppliers (name);

-- Medicine Category Enum (enforced at application layer via Java enum)
-- DosageForm Enum (enforced at application layer via Java enum)

-- Medicines Table
CREATE TABLE medicines (
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    name                 VARCHAR(200)  NOT NULL,
    generic_name         VARCHAR(200)  NOT NULL,
    category             VARCHAR(50)   NOT NULL,    -- maps to MedicineCategory enum
    dosage_form          VARCHAR(50)   NOT NULL,    -- maps to DosageForm enum
    strength             VARCHAR(50)   NOT NULL,    -- e.g. "500mg", "5mg/5ml"
    unit_of_measure      VARCHAR(30)   NOT NULL,    -- e.g. "tablet", "ml", "capsule"
    description          TEXT,
    requires_prescription BOOLEAN      DEFAULT FALSE,
    reorder_level        INT           DEFAULT 0,   -- threshold for low-stock alert
    supplier_id          BIGINT,
    active               BOOLEAN       DEFAULT TRUE,
    created_at           TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMP     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_medicine_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers(id) ON DELETE SET NULL
);

CREATE INDEX idx_medicine_name       ON medicines (name);
CREATE INDEX idx_medicine_generic    ON medicines (generic_name);
CREATE INDEX idx_medicine_category   ON medicines (category);
CREATE INDEX idx_medicine_active     ON medicines (active);

-- Seed a default supplier for development
INSERT INTO suppliers (name, contact_person, email, phone, active)
VALUES ('PharmaCo Distributors', 'Alex Turner', 'supply@pharmaco.com', '+91-9876543210', TRUE);

-- Seed sample medicines
INSERT INTO medicines (name, generic_name, category, dosage_form, strength, unit_of_measure, description, requires_prescription, reorder_level, supplier_id, active)
VALUES
('Amoxicillin 500mg', 'Amoxicillin', 'ANTIBIOTIC', 'CAPSULE', '500mg', 'capsule', 'Broad-spectrum penicillin antibiotic', TRUE, 100, 1, TRUE),
('Paracetamol 500mg', 'Paracetamol', 'ANALGESIC', 'TABLET', '500mg', 'tablet', 'Analgesic and antipyretic', FALSE, 200, 1, TRUE),
('Metformin 500mg', 'Metformin HCl', 'ANTIDIABETIC', 'TABLET', '500mg', 'tablet', 'First-line oral antidiabetic', TRUE, 150, 1, TRUE),
('Atorvastatin 10mg', 'Atorvastatin', 'CARDIOVASCULAR', 'TABLET', '10mg', 'tablet', 'HMG-CoA reductase inhibitor for cholesterol', TRUE, 100, 1, TRUE),
('ORS Sachet', 'Oral Rehydration Salts', 'ELECTROLYTE', 'SACHET', 'Standard WHO formula', 'sachet', 'Oral rehydration solution', FALSE, 50, 1, TRUE);

-- -------------------------------------------------------
-- Audit tables (Hibernate Envers)
-- -------------------------------------------------------
CREATE TABLE medicines_aud (
    id                   BIGINT NOT NULL,
    rev                  INT NOT NULL,
    revtype              TINYINT,
    name                 VARCHAR(200),
    generic_name         VARCHAR(200),
    category             VARCHAR(50),
    dosage_form          VARCHAR(50),
    strength             VARCHAR(50),
    unit_of_measure      VARCHAR(30),
    description          TEXT,
    requires_prescription BOOLEAN,
    reorder_level        INT,
    supplier_id          BIGINT,
    active               BOOLEAN,
    created_at           TIMESTAMP,
    updated_at           TIMESTAMP,
    PRIMARY KEY (id, rev),
    CONSTRAINT fk_medicine_aud_rev FOREIGN KEY (rev) REFERENCES revinfo(rev)
);
