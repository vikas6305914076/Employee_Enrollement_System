CREATE DATABASE IF NOT EXISTS employee_enrollment;
USE employee_enrollment;

CREATE TABLE IF NOT EXISTS employees (
    emp_id BIGINT NOT NULL AUTO_INCREMENT,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    username VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    department VARCHAR(100) NOT NULL,
    role VARCHAR(100) NOT NULL,
    user_role VARCHAR(20) NOT NULL,
    salary DECIMAL(12, 2) NOT NULL,
    joining_date DATE NOT NULL,
    address VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL,
    last_login_date DATETIME NULL,
    created_date DATETIME NOT NULL,
    updated_date DATETIME NOT NULL,
    CONSTRAINT pk_employees PRIMARY KEY (emp_id),
    CONSTRAINT uk_employees_username UNIQUE (username),
    CONSTRAINT uk_employees_email UNIQUE (email)
);

CREATE INDEX idx_employees_status ON employees (status);
CREATE INDEX idx_employees_department ON employees (department);
