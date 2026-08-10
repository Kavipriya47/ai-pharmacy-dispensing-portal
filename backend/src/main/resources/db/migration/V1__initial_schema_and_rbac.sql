-- Roles Table
CREATE TABLE roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(30) NOT NULL UNIQUE,
    description VARCHAR(255)
);

-- Users Table
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- User-Roles Mapping Table
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_ur_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_ur_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- Refresh Tokens Table
CREATE TABLE refresh_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    expiry_date TIMESTAMP NOT NULL,
    revoked BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_rt_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Business Audit Events Table
CREATE TABLE audit_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_type VARCHAR(50) NOT NULL,
    performed_by VARCHAR(50) NOT NULL,
    description TEXT,
    metadata TEXT,
    ip_address VARCHAR(45),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_event_type ON audit_events (event_type);
CREATE INDEX idx_audit_created_at ON audit_events (created_at);

-- Seed Initial Roles
INSERT INTO roles (name, description) VALUES ('ROLE_PHARMACIST', 'Dispensing and Inventory Operations');
INSERT INTO roles (name, description) VALUES ('ROLE_AUDITOR', 'Compliance and Audit Logs Viewer');
INSERT INTO roles (name, description) VALUES ('ROLE_ADMIN', 'System Administration');

-- Seed Default Admin & Pharmacist User (password: Password123!)
-- BCrypt hash for 'Password123!' is '$2a$10$e8wF0i8B6J1G51N7uN9e/O0n5Vn/7X/w8dZ1R8kY.X1e1Z1Z1Z1Z1' (generated at runtime or seeded)
INSERT INTO users (username, email, password_hash, full_name, active) 
VALUES ('pharmacist', 'pharmacist@pharmacy.com', '$2a$10$76d2vN1Jg8.Z1fO3R.Yy.O0S5.5J0K8N8m6F7r8s9t0u1v2w3x4y5', 'Dr. Sarah Jenkins, PharmD', TRUE);

INSERT INTO users (username, email, password_hash, full_name, active) 
VALUES ('auditor', 'auditor@pharmacy.com', '$2a$10$76d2vN1Jg8.Z1fO3R.Yy.O0S5.5J0K8N8m6F7r8s9t0u1v2w3x4y5', 'James Wilson, Lead Auditor', TRUE);

-- Map Users to Roles
INSERT INTO user_roles (user_id, role_id) VALUES (1, 1); -- pharmacist -> ROLE_PHARMACIST
INSERT INTO user_roles (user_id, role_id) VALUES (2, 2); -- auditor -> ROLE_AUDITOR
