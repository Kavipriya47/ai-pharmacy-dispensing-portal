CREATE TABLE notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    type VARCHAR(50) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    recipient VARCHAR(50),
    related_entity_type VARCHAR(100),
    related_entity_id VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

ALTER TABLE inventory ADD COLUMN reorder_level INT NOT NULL DEFAULT 50;

-- Hibernate Envers audit table must mirror the main table columns
ALTER TABLE inventory_aud ADD COLUMN reorder_level INT;
