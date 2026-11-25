-- =====================================================================
-- Finora — Manual sample data (OPTIONAL, for demo / screenshots)
-- =====================================================================
-- Execute MANUALLY in MySQL Workbench after finora_db.sql.
-- The Java application never inserts sample data automatically.
-- All transaction descriptions start with "DEMO | " so they can be
-- removed easily with the cleanup statement at the bottom.
--
-- Categories are resolved BY NAME, so this works both with the default
-- setup script and with an existing database whose category IDs differ
-- or whose names use shorter forms (e.g. "Food" / "Food & Beverage").
-- =====================================================================

USE finora_db;

SET @cat_salary   = (SELECT id FROM categories WHERE name = 'Salary' LIMIT 1);
SET @cat_freelance= (SELECT id FROM categories WHERE name = 'Freelance' LIMIT 1);
SET @cat_food     = (SELECT id FROM categories WHERE name IN ('Food & Beverage', 'Food') LIMIT 1);
SET @cat_transport= (SELECT id FROM categories WHERE name = 'Transportation' LIMIT 1);
SET @cat_shopping = (SELECT id FROM categories WHERE name = 'Shopping' LIMIT 1);
SET @cat_fun      = (SELECT id FROM categories WHERE name = 'Entertainment' LIMIT 1);
SET @cat_health   = (SELECT id FROM categories WHERE name IN ('Health', 'Healthcare') LIMIT 1);
SET @cat_education= (SELECT id FROM categories WHERE name = 'Education' LIMIT 1);
SET @cat_bills    = (SELECT id FROM categories WHERE name IN ('Bills', 'Bills & Utilities') LIMIT 1);

SET @this_month = DATE_FORMAT(CURDATE(), '%Y-%m-01');
SET @last_month = DATE_SUB(@this_month, INTERVAL 1 MONTH);
SET @two_months = DATE_SUB(@this_month, INTERVAL 2 MONTH);

-- ---------------------------------------------------------------------
-- Transactions — current month
-- ---------------------------------------------------------------------
INSERT INTO transactions (category_id, amount, description, transaction_date) VALUES
    (@cat_salary,   12500000.00, 'DEMO | Monthly salary',  DATE_ADD(@this_month, INTERVAL 1 DAY)),
    (@cat_freelance, 3250000.00, 'DEMO | Website project', DATE_ADD(@this_month, INTERVAL 7 DAY)),
    (@cat_food,      1850000.00, 'DEMO | Groceries',       DATE_ADD(@this_month, INTERVAL 2 DAY)),
    (@cat_transport,  620000.00, 'DEMO | Commute',         DATE_ADD(@this_month, INTERVAL 4 DAY)),
    (@cat_shopping,  2400000.00, 'DEMO | New laptop',      DATE_ADD(@this_month, INTERVAL 8 DAY)),
    (@cat_fun,        450000.00, 'DEMO | Streaming',       DATE_ADD(@this_month, INTERVAL 13 DAY)),
    (@cat_health,     780000.00, 'DEMO | Pharmacy',        DATE_ADD(@this_month, INTERVAL 15 DAY)),
    (@cat_education, 1100000.00, 'DEMO | Online course',   DATE_ADD(@this_month, INTERVAL 17 DAY)),
    (@cat_bills,     1275000.00, 'DEMO | Utilities',       DATE_ADD(@this_month, INTERVAL 10 DAY));

-- ---------------------------------------------------------------------
-- Transactions — previous month
-- ---------------------------------------------------------------------
INSERT INTO transactions (category_id, amount, description, transaction_date) VALUES
    (@cat_salary,   12500000.00, 'DEMO | Monthly salary',  DATE_ADD(@last_month, INTERVAL 1 DAY)),
    (@cat_food,      2150000.00, 'DEMO | Groceries',       DATE_ADD(@last_month, INTERVAL 3 DAY)),
    (@cat_transport,  540000.00, 'DEMO | Commute',         DATE_ADD(@last_month, INTERVAL 6 DAY)),
    (@cat_shopping,  1320000.00, 'DEMO | Weekend shopping',DATE_ADD(@last_month, INTERVAL 9 DAY)),
    (@cat_bills,     1200000.00, 'DEMO | Utilities',       DATE_ADD(@last_month, INTERVAL 11 DAY));

-- ---------------------------------------------------------------------
-- Transactions — two months ago
-- ---------------------------------------------------------------------
INSERT INTO transactions (category_id, amount, description, transaction_date) VALUES
    (@cat_salary,   12000000.00, 'DEMO | Monthly salary',  DATE_ADD(@two_months, INTERVAL 1 DAY)),
    (@cat_freelance, 2000000.00, 'DEMO | Side project',    DATE_ADD(@two_months, INTERVAL 6 DAY)),
    (@cat_food,      1600000.00, 'DEMO | Groceries',       DATE_ADD(@two_months, INTERVAL 4 DAY)),
    (@cat_bills,     1150000.00, 'DEMO | Utilities',       DATE_ADD(@two_months, INTERVAL 10 DAY));

-- ---------------------------------------------------------------------
-- Budgets — current month (EXPENSE categories only)
-- ---------------------------------------------------------------------
INSERT INTO budgets (category_id, amount, `month`, `year`) VALUES
    (@cat_food,      2500000.00, MONTH(CURDATE()), YEAR(CURDATE())),
    (@cat_transport,  800000.00, MONTH(CURDATE()), YEAR(CURDATE())),
    (@cat_shopping,  2000000.00, MONTH(CURDATE()), YEAR(CURDATE())),
    (@cat_fun,        500000.00, MONTH(CURDATE()), YEAR(CURDATE())),
    (@cat_health,    1000000.00, MONTH(CURDATE()), YEAR(CURDATE())),
    (@cat_education, 1200000.00, MONTH(CURDATE()), YEAR(CURDATE())),
    (@cat_bills,     1500000.00, MONTH(CURDATE()), YEAR(CURDATE()));

-- ---------------------------------------------------------------------
-- Cleanup — run this to remove all demo transactions
-- ---------------------------------------------------------------------
-- DELETE FROM transactions WHERE description LIKE 'DEMO | %';
-- DELETE FROM budgets
-- WHERE `month` = MONTH(CURDATE()) AND `year` = YEAR(CURDATE())
--   AND category_id IN (@cat_food, @cat_transport, @cat_shopping,
--                       @cat_fun, @cat_health, @cat_education, @cat_bills);

-- Note: if today is early in the month, some demo transaction dates fall
-- in the future and will not appear in the default Reports range
-- (1 January -> today). Adjust the INTERVAL values or use a later date.
