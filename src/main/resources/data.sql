-- 계좌 타입 초기 데이터
INSERT INTO account_type (code, description, transfer_fee_rate, daily_withdrawal_limit, daily_transfer_limit, created_at, updated_at)
VALUES 
    ('NORMAL', '일반계좌', 0.01, 1000000, 3000000, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('PREMIUM', '프리미엄계좌', 0.005, 5000000, 10000000, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('VIP', 'VIP계좌', 0, 10000000, 20000000, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('BUSINESS', '기업계좌', 0.002, 50000000, 100000000, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('STUDENT', '학생계좌', 0.005, 500000, 1000000, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 초기 계좌 데이터 삽입
INSERT INTO account (account_number, balance, account_type_id, status, created_at, updated_at)
VALUES 
    ('1234567891', 1000000, (SELECT id FROM account_type WHERE code = 'NORMAL'), 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('2345678910', 1000000, (SELECT id FROM account_type WHERE code = 'PREMIUM'), 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP); 