-- Transaction 시퀀스 생성
CREATE SEQUENCE IF NOT EXISTS transaction_seq START WITH 1 INCREMENT BY 1;

-- 계좌 타입 초기 데이터
INSERT INTO account_type (id, code, description, transfer_fee_rate, daily_withdrawal_limit, daily_transfer_limit, active, created_at, updated_at)
VALUES 
    (1, 'NORMAL', '일반 계좌', 0.01, 1000000, 2000000, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 'VIP', 'VIP 계좌', 0.005, 5000000, 10000000, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);