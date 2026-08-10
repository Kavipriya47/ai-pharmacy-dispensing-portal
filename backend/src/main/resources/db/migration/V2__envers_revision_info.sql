-- Hibernate Envers Global Revision Info Table
CREATE TABLE revinfo (
    rev INT AUTO_INCREMENT PRIMARY KEY,
    revtstmp BIGINT
);

-- Hibernate Envers Audit Table for Users
CREATE TABLE users_AUD (
    id BIGINT NOT NULL,
    rev INT NOT NULL,
    revtype TINYINT,
    username VARCHAR(50),
    email VARCHAR(100),
    full_name VARCHAR(100),
    active BOOLEAN,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    PRIMARY KEY (id, rev),
    CONSTRAINT fk_users_aud_rev FOREIGN KEY (rev) REFERENCES revinfo(rev)
);
