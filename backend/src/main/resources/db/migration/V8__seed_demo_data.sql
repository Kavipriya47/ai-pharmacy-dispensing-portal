-- ============================================================
-- V8: Seed Demo Data for Presentation
-- NOTE: This migration is strictly for deploying the demo
-- environment. It seeds inventory, batches, and 30 days of
-- historical dispensing records to enable AI Forecasting,
-- FEFO, Expiry, and Recall demonstrations.
-- ============================================================

-- ------------------------------------------------------------
-- 1. Create Inventory Records for Medicines (IDs 1 to 5)
-- ------------------------------------------------------------
INSERT INTO inventory (medicine_id, total_quantity) VALUES
(1, 1500), -- Amoxicillin (High demand)
(2, 2000), -- Paracetamol (Moderate/increasing)
(3, 50),   -- Metformin (Low stock to trigger procurement alert)
(4, 500),  -- Atorvastatin (For Recall demo)
(5, 100);  -- ORS Sachet

-- ------------------------------------------------------------
-- 2. Create Medicine Batches
-- ------------------------------------------------------------
-- Medicine 1 (Amoxicillin, Inv 1)
-- Batch 1A: Healthy expiry
INSERT INTO medicine_batches (inventory_id, batch_number, manufacturer, manufacturing_date, expiry_date, quantity_received, quantity_remaining, unit_cost, status)
VALUES (1, 'DEMO-AMOX-A1', 'PharmaCo', DATE_SUB(CURRENT_DATE, INTERVAL 100 DAY), DATE_ADD(CURRENT_DATE, INTERVAL 365 DAY), 1000, 1000, 1.50, 'ACTIVE');

-- Batch 1B: Near expiry (within 30 days) to trigger Expiry-Waste risk
INSERT INTO medicine_batches (inventory_id, batch_number, manufacturer, manufacturing_date, expiry_date, quantity_received, quantity_remaining, unit_cost, status)
VALUES (1, 'DEMO-AMOX-B1', 'PharmaCo', DATE_SUB(CURRENT_DATE, INTERVAL 700 DAY), DATE_ADD(CURRENT_DATE, INTERVAL 15 DAY), 500, 500, 1.50, 'ACTIVE');

-- Medicine 2 (Paracetamol, Inv 2)
-- Multiple batches to demonstrate FEFO
INSERT INTO medicine_batches (inventory_id, batch_number, manufacturer, manufacturing_date, expiry_date, quantity_received, quantity_remaining, unit_cost, status)
VALUES (2, 'DEMO-PARA-A1', 'PharmaCo', DATE_SUB(CURRENT_DATE, INTERVAL 200 DAY), DATE_ADD(CURRENT_DATE, INTERVAL 100 DAY), 1000, 1000, 0.50, 'ACTIVE');

INSERT INTO medicine_batches (inventory_id, batch_number, manufacturer, manufacturing_date, expiry_date, quantity_received, quantity_remaining, unit_cost, status)
VALUES (2, 'DEMO-PARA-B1', 'PharmaCo', DATE_SUB(CURRENT_DATE, INTERVAL 100 DAY), DATE_ADD(CURRENT_DATE, INTERVAL 300 DAY), 1000, 1000, 0.50, 'ACTIVE');

-- Medicine 3 (Metformin, Inv 3) - Low Stock
INSERT INTO medicine_batches (inventory_id, batch_number, manufacturer, manufacturing_date, expiry_date, quantity_received, quantity_remaining, unit_cost, status)
VALUES (3, 'DEMO-METF-A1', 'PharmaCo', DATE_SUB(CURRENT_DATE, INTERVAL 300 DAY), DATE_ADD(CURRENT_DATE, INTERVAL 400 DAY), 500, 50, 2.00, 'ACTIVE');

-- Medicine 4 (Atorvastatin, Inv 4) - RECALLED BATCH
INSERT INTO medicine_batches (inventory_id, batch_number, manufacturer, manufacturing_date, expiry_date, quantity_received, quantity_remaining, unit_cost, status)
VALUES (4, 'DEMO-ATOR-RECALL-001', 'PharmaCo', DATE_SUB(CURRENT_DATE, INTERVAL 150 DAY), DATE_ADD(CURRENT_DATE, INTERVAL 500 DAY), 1000, 500, 3.50, 'RECALLED');

-- Medicine 5 (ORS, Inv 5)
INSERT INTO medicine_batches (inventory_id, batch_number, manufacturer, manufacturing_date, expiry_date, quantity_received, quantity_remaining, unit_cost, status)
VALUES (5, 'DEMO-ORS-A1', 'PharmaCo', DATE_SUB(CURRENT_DATE, INTERVAL 50 DAY), DATE_ADD(CURRENT_DATE, INTERVAL 600 DAY), 100, 100, 0.25, 'ACTIVE');


-- ------------------------------------------------------------
-- 3. Create Inventory Transactions for Initial Stock
-- ------------------------------------------------------------
INSERT INTO inventory_transactions (batch_id, transaction_type, quantity, performed_by, notes)
SELECT id, 'RECEIPT', quantity_received, 'admin', 'Initial Demo Stock' FROM medicine_batches;


-- ------------------------------------------------------------
-- 4. Create Historical Dispensing Records (30 Days) for AI
-- ------------------------------------------------------------
-- We will use a recursive CTE to generate 30 days of dates if supported by MySQL 8,
-- but to be safe and compatible with standard MySQL, we insert them manually or use cross joins.
-- Alternatively, simple explicit inserts for the last 30 days.

-- To keep the script simple and compatible, we explicitly insert records.
-- We use a stored procedure to loop and insert data.

DELIMITER //

CREATE PROCEDURE SeedHistoricalDispensing()
BEGIN
    DECLARE i INT DEFAULT 30;
    DECLARE cur_date TIMESTAMP;
    
    WHILE i > 0 DO
        SET cur_date = DATE_SUB(CURRENT_TIMESTAMP, INTERVAL i DAY);
        
        -- Amoxicillin (Medicine 1, Batch 1 - id 1) -> High demand, approx 50/day
        INSERT INTO dispensation_records (medicine_id, batch_id, patient_identifier, prescription_reference, quantity_dispensed, dispensed_by, dispensed_at)
        VALUES (1, 1, CONCAT('PATIENT-AMOX-', i), 'RX-DEMO-001', 50, 'pharmacist', cur_date);
        
        -- Paracetamol (Medicine 2, Batch 3 - id 3) -> Increasing demand, (30 - i)*2
        INSERT INTO dispensation_records (medicine_id, batch_id, patient_identifier, prescription_reference, quantity_dispensed, dispensed_by, dispensed_at)
        VALUES (2, 3, CONCAT('PATIENT-PARA-', i), NULL, 10 + ((30 - i) * 2), 'pharmacist', cur_date);
        
        -- Metformin (Medicine 3, Batch 5 - id 5) -> Low/stable demand, 5/day
        INSERT INTO dispensation_records (medicine_id, batch_id, patient_identifier, prescription_reference, quantity_dispensed, dispensed_by, dispensed_at)
        VALUES (3, 5, CONCAT('PATIENT-METF-', i), 'RX-DEMO-002', 5, 'pharmacist', cur_date);
        
        SET i = i - 1;
    END WHILE;
    
    -- Insert exactly ONE dispensation for the RECALLED batch (Atorvastatin, Batch 6 - id 6) 10 days ago
    INSERT INTO dispensation_records (medicine_id, batch_id, patient_identifier, prescription_reference, quantity_dispensed, dispensed_by, dispensed_at)
    VALUES (4, 6, 'PATIENT-RECALL-TRACER', 'RX-DEMO-999', 30, 'pharmacist', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 10 DAY));
END //

DELIMITER ;

-- Call the procedure to generate the records
CALL SeedHistoricalDispensing();

-- Clean up
DROP PROCEDURE SeedHistoricalDispensing;
