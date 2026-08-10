-- V7: Seed/fix default authentication accounts
-- LOCAL/DEMO credentials only.
-- Password: Password123!

-- 1. Fix existing pharmacist password
UPDATE users
SET password_hash = '$2b$12$LRzvgpis8gcZfdd.eEmYTe5Npj/aQAbHPYws41MxTthb2nAsxQlxy'
WHERE username = 'pharmacist';

-- 2. Fix existing auditor password
UPDATE users
SET password_hash = '$2b$12$LRzvgpis8gcZfdd.eEmYTe5Npj/aQAbHPYws41MxTthb2nAsxQlxy'
WHERE username = 'auditor';

-- 3. Create ADMIN if it does not already exist
INSERT INTO users (
    username,
    email,
    password_hash,
    full_name,
    active
)
SELECT
    'admin',
    'admin@pharmacy.com',
    '$2b$12$LRzvgpis8gcZfdd.eEmYTe5Npj/aQAbHPYws41MxTthb2nAsxQlxy',
    'System Administrator',
    TRUE
WHERE NOT EXISTS (
    SELECT 1
    FROM users
    WHERE username = 'admin'
);

-- 4. Ensure pharmacist has PHARMACIST role
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
JOIN roles r ON r.name = 'ROLE_PHARMACIST'
WHERE u.username = 'pharmacist'
  AND NOT EXISTS (
      SELECT 1
      FROM user_roles ur
      WHERE ur.user_id = u.id
        AND ur.role_id = r.id
  );

-- 5. Ensure auditor has AUDITOR role
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
JOIN roles r ON r.name = 'ROLE_AUDITOR'
WHERE u.username = 'auditor'
  AND NOT EXISTS (
      SELECT 1
      FROM user_roles ur
      WHERE ur.user_id = u.id
        AND ur.role_id = r.id
  );

-- 6. Ensure admin has ADMIN role
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
JOIN roles r ON r.name = 'ROLE_ADMIN'
WHERE u.username = 'admin'
  AND NOT EXISTS (
      SELECT 1
      FROM user_roles ur
      WHERE ur.user_id = u.id
        AND ur.role_id = r.id
  );
