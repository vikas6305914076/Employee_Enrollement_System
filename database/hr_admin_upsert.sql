-- Creates or refreshes the default admin login used for Railway deployment checks.
-- Username: hr.admin
-- Password: Admin@123
-- The password is stored as a BCrypt hash because the application does not accept plain-text passwords.

UPDATE employees
SET
    first_name = 'System',
    last_name = 'Admin',
    email = 'admin@ems.local',
    phone = '9876543210',
    department = 'Human Resources',
    role = 'HR Administrator',
    user_role = 'ADMIN',
    salary = 95000.00,
    joining_date = '2024-01-01',
    address = 'Corporate HQ, Bengaluru',
    password = '$2a$10$x8hSQ9SmyYkLeOH3QwRNPe0XkDvj39fxgoprH8gpr3kwOaHLUCFgW',
    status = 'ACTIVE',
    updated_date = NOW()
WHERE username = 'hr.admin';

INSERT INTO employees (
    first_name,
    last_name,
    username,
    email,
    phone,
    department,
    role,
    user_role,
    salary,
    joining_date,
    address,
    password,
    status,
    last_login_date,
    created_date,
    updated_date
)
SELECT
    'System',
    'Admin',
    'hr.admin',
    'admin@ems.local',
    '9876543210',
    'Human Resources',
    'HR Administrator',
    'ADMIN',
    95000.00,
    '2024-01-01',
    'Corporate HQ, Bengaluru',
    '$2a$10$x8hSQ9SmyYkLeOH3QwRNPe0XkDvj39fxgoprH8gpr3kwOaHLUCFgW',
    'ACTIVE',
    NULL,
    NOW(),
    NOW()
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1
    FROM employees
    WHERE username = 'hr.admin'
);

SELECT emp_id, username, email, user_role, status
FROM employees
WHERE username = 'hr.admin';
