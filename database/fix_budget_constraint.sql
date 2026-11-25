-- =====================================================================
-- Finora — Manual fix for an older budgets table (OPTIONAL)
-- =====================================================================
-- Some older installations created:
--     UNIQUE KEY category_id (category_id)
-- which prevents one category from having budgets in different months.
-- The agreed design requires:
--     UNIQUE KEY (category_id, month, year)
--
-- Run this script ONLY if step 1 below reports the old single-column key.
-- The current development database already has the correct constraint,
-- so this script is kept for reference / other machines.
-- =====================================================================

USE finora_db;

-- ---------------------------------------------------------------------
-- 1. Verify the current constraint first
-- ---------------------------------------------------------------------
SHOW CREATE TABLE budgets;
-- Correct  -> UNIQUE KEY `category_id` (`category_id`,`month`,`year`)
-- Outdated -> UNIQUE KEY `category_id` (`category_id`)

-- ---------------------------------------------------------------------
-- 2. Make sure no duplicate (category, month, year) rows exist
--    (the query must return 0 rows before continuing)
-- ---------------------------------------------------------------------
SELECT category_id, `month`, `year`, COUNT(*) AS total
FROM budgets
GROUP BY category_id, `month`, `year`
HAVING COUNT(*) > 1;

-- ---------------------------------------------------------------------
-- 3. Replace the constraint
-- ---------------------------------------------------------------------
ALTER TABLE budgets
    ADD CONSTRAINT uq_budgets_category_month_year
    UNIQUE (category_id, `month`, `year`);

ALTER TABLE budgets DROP INDEX category_id;

-- If MySQL refuses to drop the index because of the foreign key, run
-- this instead of the two statements above:
--
-- ALTER TABLE budgets DROP FOREIGN KEY budgets_ibfk_1;
-- ALTER TABLE budgets DROP INDEX category_id;
-- ALTER TABLE budgets
--     ADD CONSTRAINT budgets_ibfk_1
--     FOREIGN KEY (category_id) REFERENCES categories(id);

-- ---------------------------------------------------------------------
-- 4. Verify again
-- ---------------------------------------------------------------------
SHOW CREATE TABLE budgets;
