-- =========================================================================
-- V2: Seed Data (Demo Users, Customers, Accounts, 60-Day Synthetic Transactions)
-- Password for all demo accounts is: Password123!
-- =========================================================================

-- 1. Users
INSERT INTO users (id, username, password_hash, email, full_name, role, enabled)
VALUES
    ('11111111-1111-1111-1111-111111111111', 'admin', '$2a$10$1lH3ueg5JzDWNwkUL1m4bOWJpYYUUj30iKQHHUXZX0ELg2yYGux92', 'admin@finassist.co.uk', 'System Administrator', 'ROLE_ADMIN', true),
    ('22222222-2222-2222-2222-222222222222', 'oliver', '$2a$10$1lH3ueg5JzDWNwkUL1m4bOWJpYYUUj30iKQHHUXZX0ELg2yYGux92', 'oliver.twist@gmail.com', 'Oliver Twist', 'ROLE_CUSTOMER', true),
    ('33333333-3333-3333-3333-333333333333', 'emma', '$2a$10$1lH3ueg5JzDWNwkUL1m4bOWJpYYUUj30iKQHHUXZX0ELg2yYGux92', 'emma.watson@oxford.ac.uk', 'Emma Watson', 'ROLE_CUSTOMER', true);

-- 2. Customers
INSERT INTO customers (id, user_id, customer_number, first_name, last_name, email, phone_number, home_city, currency)
VALUES
    ('c0000001-0000-0000-0000-000000000001', '22222222-2222-2222-2222-222222222222', 'CUST-UK-1001', 'Oliver', 'Twist', 'oliver.twist@gmail.com', '+447911123456', 'London', 'GBP'),
    ('c0000002-0000-0000-0000-000000000002', '33333333-3333-3333-3333-333333333333', 'CUST-UK-1002', 'Emma', 'Watson', 'emma.watson@oxford.ac.uk', '+447922234567', 'Oxford', 'GBP');

-- 3. Accounts
INSERT INTO accounts (id, customer_id, account_number, sort_code, account_type, currency, balance, status)
VALUES
    ('a0000001-0000-0000-0000-000000000001', 'c0000001-0000-0000-0000-000000000001', '12345678', '204514', 'CURRENT', 'GBP', 15420.50, 'ACTIVE'),
    ('a0000002-0000-0000-0000-000000000002', 'c0000002-0000-0000-0000-000000000002', '87654321', '400530', 'CURRENT', 'GBP', 8920.00, 'ACTIVE');

-- 4. 60-Day Transactions for Oliver Twist (c0000001-...)
-- July 2026 Transactions (Base Month)
INSERT INTO transactions (id, account_id, customer_id, amount, currency, direction, category, merchant_name, description, is_recurring, transaction_time)
VALUES
    ('d0000001-0000-0000-0000-000000000001', 'a0000001-0000-0000-0000-000000000001', 'c0000001-0000-0000-0000-000000000001', 1450.00, 'GBP', 'DEBIT', 'Rent', 'London Residential Properties', 'Monthly Apartment Rent', true, '2026-07-01 09:00:00+00'),
    ('d0000002-0000-0000-0000-000000000002', 'a0000001-0000-0000-0000-000000000001', 'c0000001-0000-0000-0000-000000000001', 17.99, 'GBP', 'DEBIT', 'Entertainment', 'Netflix UK', 'Monthly Premium Streaming Subscription', true, '2026-07-02 08:30:00+00'),
    ('d0000003-0000-0000-0000-000000000003', 'a0000001-0000-0000-0000-000000000001', 'c0000001-0000-0000-0000-000000000001', 10.99, 'GBP', 'DEBIT', 'Entertainment', 'Spotify UK', 'Monthly Music Family Plan', true, '2026-07-03 08:30:00+00'),
    ('d0000004-0000-0000-0000-000000000004', 'a0000001-0000-0000-0000-000000000001', 'c0000001-0000-0000-0000-000000000001', 55.00, 'GBP', 'DEBIT', 'Entertainment', 'PureGym London', 'Monthly Gym Membership', true, '2026-07-05 06:00:00+00'),
    ('d0000005-0000-0000-0000-000000000005', 'a0000001-0000-0000-0000-000000000001', 'c0000001-0000-0000-0000-000000000001', 84.50, 'GBP', 'DEBIT', 'Groceries', 'Tesco Stores London', 'Weekly Grocery Shopping', false, '2026-07-06 14:15:00+00'),
    ('d0000006-0000-0000-0000-000000000006', 'a0000001-0000-0000-0000-000000000001', 'c0000001-0000-0000-0000-000000000001', 65.00, 'GBP', 'DEBIT', 'Transport', 'Transport for London', 'Weekly Tube Commute Card', false, '2026-07-08 18:30:00+00'),
    ('d0000007-0000-0000-0000-000000000007', 'a0000001-0000-0000-0000-000000000001', 'c0000001-0000-0000-0000-000000000001', 120.00, 'GBP', 'DEBIT', 'Dining', 'Dishoom Covent Garden', 'Dinner with Friends', false, '2026-07-12 20:00:00+00'),
    ('d0000008-0000-0000-0000-000000000008', 'a0000001-0000-0000-0000-000000000001', 'c0000001-0000-0000-0000-000000000001', 92.00, 'GBP', 'DEBIT', 'Groceries', 'Sainsbury''s Superstore', 'Groceries and Household', false, '2026-07-16 11:30:00+00'),
    ('d0000009-0000-0000-0000-000000000009', 'a0000001-0000-0000-0000-000000000001', 'c0000001-0000-0000-0000-000000000001', 110.00, 'GBP', 'DEBIT', 'Utilities', 'British Gas Energy', 'Monthly Gas & Electricity Bill', true, '2026-07-20 09:00:00+00'),
    ('d0000010-0000-0000-0000-000000000010', 'a0000001-0000-0000-0000-000000000001', 'c0000001-0000-0000-0000-000000000001', 150.00, 'GBP', 'DEBIT', 'Dining', 'Nando''s & The Ivy', 'Weekend Dining', false, '2026-07-25 19:45:00+00'),
    ('d0000011-0000-0000-0000-000000000011', 'a0000001-0000-0000-0000-000000000001', 'c0000001-0000-0000-0000-000000000001', 4200.00, 'GBP', 'CREDIT', 'Income', 'FinTech Global Ltd', 'Monthly Employment Salary', true, '2026-07-28 07:00:00+00');

-- August 2026 Transactions (Higher Spend Month - Dining +£420, Shopping +£350, Transport +£80)
INSERT INTO transactions (id, account_id, customer_id, amount, currency, direction, category, merchant_name, description, is_recurring, transaction_time)
VALUES
    ('d0000012-0000-0000-0000-000000000012', 'a0000001-0000-0000-0000-000000000001', 'c0000001-0000-0000-0000-000000000001', 1450.00, 'GBP', 'DEBIT', 'Rent', 'London Residential Properties', 'Monthly Apartment Rent', true, '2026-08-01 09:00:00+00'),
    ('d0000013-0000-0000-0000-000000000013', 'a0000001-0000-0000-0000-000000000001', 'c0000001-0000-0000-0000-000000000001', 17.99, 'GBP', 'DEBIT', 'Entertainment', 'Netflix UK', 'Monthly Premium Streaming Subscription', true, '2026-08-02 08:30:00+00'),
    ('d0000014-0000-0000-0000-000000000014', 'a0000001-0000-0000-0000-000000000001', 'c0000001-0000-0000-0000-000000000001', 10.99, 'GBP', 'DEBIT', 'Entertainment', 'Spotify UK', 'Monthly Music Family Plan', true, '2026-08-03 08:30:00+00'),
    ('d0000015-0000-0000-0000-000000000015', 'a0000001-0000-0000-0000-000000000001', 'c0000001-0000-0000-0000-000000000001', 55.00, 'GBP', 'DEBIT', 'Entertainment', 'PureGym London', 'Monthly Gym Membership', true, '2026-08-05 06:00:00+00'),
    ('d0000016-0000-0000-0000-000000000016', 'a0000001-0000-0000-0000-000000000001', 'c0000001-0000-0000-0000-000000000001', 115.00, 'GBP', 'DEBIT', 'Groceries', 'Waitrose & Partners', 'Specialty Groceries', false, '2026-08-07 16:30:00+00'),
    ('d0000017-0000-0000-0000-000000000017', 'a0000001-0000-0000-0000-000000000001', 'c0000001-0000-0000-0000-000000000001', 320.00, 'GBP', 'DEBIT', 'Dining', 'Hawksmoor Steakhouse', 'Celebration Dinner', false, '2026-08-10 20:30:00+00'),
    ('d0000018-0000-0000-0000-000000000018', 'a0000001-0000-0000-0000-000000000001', 'c0000001-0000-0000-0000-000000000001', 270.00, 'GBP', 'DEBIT', 'Dining', 'Sketch London', 'Weekend Tasting Menu', false, '2026-08-14 21:00:00+00'),
    ('d0000019-0000-0000-0000-000000000019', 'a0000001-0000-0000-0000-000000000001', 'c0000001-0000-0000-0000-000000000001', 350.00, 'GBP', 'DEBIT', 'Shopping', 'Apple Regent Street', 'Accessories & Electronics', false, '2026-08-17 14:00:00+00'),
    ('d0000020-0000-0000-0000-000000000020', 'a0000001-0000-0000-0000-000000000001', 'c0000001-0000-0000-0000-000000000001', 145.00, 'GBP', 'DEBIT', 'Transport', 'Uber & Heathrow Express', 'Airport Transportation', false, '2026-08-20 06:45:00+00'),
    ('d0000021-0000-0000-0000-000000000021', 'a0000001-0000-0000-0000-000000000001', 'c0000001-0000-0000-0000-000000000001', 110.00, 'GBP', 'DEBIT', 'Utilities', 'British Gas Energy', 'Monthly Gas & Electricity Bill', true, '2026-08-20 09:00:00+00'),
    ('d0000022-0000-0000-0000-000000000022', 'a0000001-0000-0000-0000-000000000001', 'c0000001-0000-0000-0000-000000000001', 4200.00, 'GBP', 'CREDIT', 'Income', 'FinTech Global Ltd', 'Monthly Employment Salary', true, '2026-08-25 07:00:00+00');
