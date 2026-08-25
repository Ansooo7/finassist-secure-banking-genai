-- =========================================================================
-- V1: Initial Database Schema (PostgreSQL 16 / pgvector / H2 compatible)
-- =========================================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 1. Users
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role VARCHAR(30) NOT NULL DEFAULT 'ROLE_CUSTOMER',
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 2. Customers
CREATE TABLE IF NOT EXISTS customers (
    id UUID PRIMARY KEY,
    user_id UUID UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    customer_number VARCHAR(30) UNIQUE NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL,
    phone_number VARCHAR(25),
    home_city VARCHAR(50) DEFAULT 'London',
    currency VARCHAR(3) DEFAULT 'GBP',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 3. Bank Accounts
CREATE TABLE IF NOT EXISTS accounts (
    id UUID PRIMARY KEY,
    customer_id UUID NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
    account_number VARCHAR(8) UNIQUE NOT NULL,
    sort_code VARCHAR(6) NOT NULL DEFAULT '204514',
    account_type VARCHAR(30) NOT NULL DEFAULT 'CURRENT',
    currency VARCHAR(3) NOT NULL DEFAULT 'GBP',
    balance DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 4. Transactions
CREATE TABLE IF NOT EXISTS transactions (
    id UUID PRIMARY KEY,
    account_id UUID NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
    customer_id UUID NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
    amount DECIMAL(12, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'GBP',
    direction VARCHAR(10) NOT NULL, -- DEBIT or CREDIT
    category VARCHAR(50) NOT NULL, -- Dining, Groceries, Transport, Entertainment, Utilities, Rent, Shopping, Income
    merchant_name VARCHAR(100) NOT NULL,
    description TEXT,
    is_recurring BOOLEAN DEFAULT FALSE,
    transaction_time TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 5. Conversation Sessions
CREATE TABLE IF NOT EXISTS conversation_sessions (
    id UUID PRIMARY KEY,
    customer_id UUID NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
    session_title VARCHAR(150),
    started_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    last_activity_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 6. Chat Messages
CREATE TABLE IF NOT EXISTS chat_messages (
    id UUID PRIMARY KEY,
    session_id UUID NOT NULL REFERENCES conversation_sessions(id) ON DELETE CASCADE,
    customer_id UUID NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
    sender VARCHAR(20) NOT NULL, -- USER, ASSISTANT
    message_text TEXT NOT NULL,
    intent_detected VARCHAR(50),
    guardrail_status VARCHAR(30) DEFAULT 'PASSED',
    data_points_summary TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 7. AI Audit Logs
CREATE TABLE IF NOT EXISTS ai_audit_logs (
    id UUID PRIMARY KEY,
    customer_id UUID REFERENCES customers(id),
    username VARCHAR(50),
    correlation_id VARCHAR(100) NOT NULL,
    prompt_sanitized TEXT NOT NULL,
    intent VARCHAR(50),
    guardrail_status VARCHAR(30) NOT NULL,
    guardrail_details TEXT,
    retrieved_sources TEXT,
    latency_ms INT,
    response_status VARCHAR(20),
    timestamp TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
