-- Create database if not exists
CREATE DATABASE IF NOT EXISTS mydb;
USE mydb;

-- Create sequences
CREATE SEQUENCE IF NOT EXISTS transaction_seq START WITH 41 INCREMENT BY 1;

-- Create account type table
CREATE TABLE IF NOT EXISTS account_type (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,
    description VARCHAR(100) NOT NULL,
    transfer_fee_rate DECIMAL(5,4) NOT NULL,
    daily_withdrawal_limit DECIMAL(19,0) NOT NULL,
    daily_transfer_limit DECIMAL(19,0) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Create account table
CREATE TABLE IF NOT EXISTS account (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    account_number VARCHAR(20) NOT NULL UNIQUE,
    account_type_id BIGINT NOT NULL,
    balance DECIMAL(19,0) NOT NULL DEFAULT 0,
    status VARCHAR(10) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    CONSTRAINT fk_account_type FOREIGN KEY (account_type_id) REFERENCES account_type(id),
    CONSTRAINT chk_account_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'DELETED'))
);

-- Create transaction table
CREATE TABLE IF NOT EXISTS transaction (
    id BIGINT PRIMARY KEY,
    account_id BIGINT NOT NULL,
    type VARCHAR(20) NOT NULL,
    amount DECIMAL(19,0) NOT NULL,
    balance_after_transaction DECIMAL(19,0) NOT NULL,
    fee DECIMAL(19,2),
    related_account_number VARCHAR(20),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_transaction_account FOREIGN KEY (account_id) REFERENCES account(id),
    CONSTRAINT chk_transaction_type CHECK (type IN ('DEPOSIT', 'WITHDRAWAL', 'TRANSFER_OUT', 'TRANSFER_IN'))
);

-- Insert account types
INSERT INTO account_type (code, description, transfer_fee_rate, daily_withdrawal_limit, daily_transfer_limit, active) VALUES
('NORMAL', '일반 계좌', 0.01, 1000000, 2000000, true),
('VIP', 'VIP 계좌', 0.005, 5000000, 10000000, true);

-- Insert test accounts
INSERT INTO account (account_number, account_type_id, balance, status) VALUES
('1234567890', 1, 1000000, 'ACTIVE'),  -- 일반 계좌
('2345678901', 2, 5000000, 'ACTIVE');  -- VIP 계좌

-- Insert test transactions for first account (일반 계좌)
INSERT INTO transaction (id, account_id, type, amount, balance_after_transaction, fee, related_account_number)
WITH RECURSIVE numbers AS (
    SELECT 1 as n
    UNION ALL
    SELECT n + 1 FROM numbers WHERE n < 20
)
SELECT 
    n as id,
    1 as account_id,
    CASE 
        WHEN n % 4 = 0 THEN 'WITHDRAWAL'
        WHEN n % 4 = 1 THEN 'DEPOSIT'
        WHEN n % 4 = 2 THEN 'TRANSFER_OUT'
        ELSE 'TRANSFER_IN'
    END as type,
    CASE 
        WHEN n % 4 = 0 THEN 10000
        WHEN n % 4 = 1 THEN 15000
        WHEN n % 4 = 2 THEN 20000
        ELSE 25000
    END as amount,
    1000000 + (n * 5000) as balance_after_transaction,
    CASE 
        WHEN n % 4 IN (2, 3) THEN 200.00
        ELSE NULL
    END as fee,
    CASE 
        WHEN n % 4 IN (2, 3) THEN '2345678901'
        ELSE NULL
    END as related_account_number
FROM numbers;

-- Insert test transactions for second account (VIP 계좌)
INSERT INTO transaction (id, account_id, type, amount, balance_after_transaction, fee, related_account_number)
WITH RECURSIVE numbers AS (
    SELECT 21 as n
    UNION ALL
    SELECT n + 1 FROM numbers WHERE n < 40
)
SELECT 
    n as id,
    2 as account_id,
    CASE 
        WHEN n % 4 = 0 THEN 'WITHDRAWAL'
        WHEN n % 4 = 1 THEN 'DEPOSIT'
        WHEN n % 4 = 2 THEN 'TRANSFER_OUT'
        ELSE 'TRANSFER_IN'
    END as type,
    CASE 
        WHEN n % 4 = 0 THEN 50000
        WHEN n % 4 = 1 THEN 75000
        WHEN n % 4 = 2 THEN 100000
        ELSE 125000
    END as amount,
    5000000 + ((n-20) * 25000) as balance_after_transaction,
    CASE 
        WHEN n % 4 IN (2, 3) THEN 500.00
        ELSE NULL
    END as fee,
    CASE 
        WHEN n % 4 IN (2, 3) THEN '1234567890'
        ELSE NULL
    END as related_account_number
FROM numbers; 