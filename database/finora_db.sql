-- =====================================================================
-- Finora — Manual database setup script
-- =====================================================================
-- Execute this script MANUALLY in MySQL Workbench (or mysql CLI).
-- The Java application NEVER creates or modifies the schema.
-- Intended for a fresh database. Review before running on an existing one.
-- =====================================================================

CREATE DATABASE IF NOT EXISTS finora_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE finora_db;

-- ---------------------------------------------------------------------
-- 1. categories
-- ---------------------------------------------------------------------
CREATE TABLE categories (
    id    INT          NOT NULL AUTO_INCREMENT,
    name  VARCHAR(100) NOT NULL,
    type  ENUM('INCOME', 'EXPENSE') NOT NULL,
    icon  VARCHAR(50)  NULL,
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- 2. transactions
-- ---------------------------------------------------------------------
CREATE TABLE transactions (
    id               INT           NOT NULL AUTO_INCREMENT,
    category_id      INT           NOT NULL,
    amount           DECIMAL(15,2) NOT NULL,
    description      VARCHAR(255)  NULL,
    transaction_date DATE          NOT NULL,
    created_at       TIMESTAMP     NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_transactions_category (category_id),
    KEY idx_transactions_date (transaction_date),
    CONSTRAINT fk_transactions_category
        FOREIGN KEY (category_id) REFERENCES categories (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- 3. budgets
-- ---------------------------------------------------------------------
-- One budget per category per month/year (agreed design).
CREATE TABLE budgets (
    id          INT           NOT NULL AUTO_INCREMENT,
    category_id INT           NOT NULL,
    amount      DECIMAL(15,2) NOT NULL,
    `month`     INT           NOT NULL,
    `year`      INT           NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_budgets_category_month_year (category_id, `month`, `year`),
    KEY idx_budgets_category (category_id),
    CONSTRAINT fk_budgets_category
        FOREIGN KEY (category_id) REFERENCES categories (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- 4. Default categories
-- ---------------------------------------------------------------------
INSERT INTO categories (id, name, type) VALUES
    (1,  'Salary',           'INCOME'),
    (2,  'Freelance',        'INCOME'),
    (3,  'Business',         'INCOME'),
    (4,  'Investment',       'INCOME'),
    (5,  'Other Income',     'INCOME'),
    (6,  'Food & Beverage',  'EXPENSE'),
    (7,  'Transportation',   'EXPENSE'),
    (8,  'Shopping',         'EXPENSE'),
    (9,  'Entertainment',    'EXPENSE'),
    (10, 'Health',           'EXPENSE'),
    (11, 'Education',        'EXPENSE'),
    (12, 'Bills',            'EXPENSE'),
    (13, 'Other Expenses',   'EXPENSE');

-- Optional demo records: run database/sample_data.sql separately.
