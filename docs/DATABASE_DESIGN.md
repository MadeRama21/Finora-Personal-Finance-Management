# Finora — Database Design

## 1. Database Management

Database management is separate from the Java application.

The developer is responsible for creating and managing the database using MySQL Workbench or MySQL tools.

| Property | Value |
|---|---|
| DBMS | MySQL 8 |
| Database Name | finora_db |
| Connection | JDBC |
| Main Tables | categories, transactions, budgets |

### Important Rules

- Create the database manually in MySQL Workbench.
- Create all tables manually in MySQL.
- Java only connects to the existing database.
- Java uses JDBC to perform database operations.
- The application must not automatically create databases or tables.
- Do not use Hibernate, JPA, or other ORM frameworks.
- Do not introduce additional tables without approval.

## 2. Database Tables

Finora version 1 uses three main tables.

### 2.1 categories

Purpose: Store income and expense categories.

| Column | Data Type | Constraint |
|---|---|---|
| id | INT | PRIMARY KEY, AUTO_INCREMENT |
| name | VARCHAR(100) | NOT NULL |
| type | ENUM('INCOME','EXPENSE') | NOT NULL |
| icon | VARCHAR(50) | NULL |

Examples:

| id | name | type |
|---|---|---|
| 1 | Salary | INCOME |
| 2 | Freelance | INCOME |
| 3 | Food | EXPENSE |
| 4 | Transportation | EXPENSE |
| 5 | Shopping | EXPENSE |

### 2.2 transactions

Purpose: Store income and expense records.

| Column | Data Type | Constraint |
|---|---|---|
| id | INT | PRIMARY KEY, AUTO_INCREMENT |
| category_id | INT | FOREIGN KEY, NOT NULL |
| amount | DECIMAL(15,2) | NOT NULL |
| description | VARCHAR(255) | NULL |
| transaction_date | DATE | NOT NULL |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP |

Foreign key:

`category_id` references `categories(id)`.

### 2.3 budgets

Purpose: Store monthly spending limits.

| Column | Data Type | Constraint |
|---|---|---|
| id | INT | PRIMARY KEY, AUTO_INCREMENT |
| category_id | INT | FOREIGN KEY, NOT NULL |
| amount | DECIMAL(15,2) | NOT NULL |
| month | INT | NOT NULL |
| year | INT | NOT NULL |

Rules:
- Month must be between 1 and 12.
- Amount must be greater than zero.
- Category must be an EXPENSE category.
- A category can only have one budget per month.

Add this unique constraint:

`UNIQUE(category_id, month, year)`.

## 3. Table Relationships

```text
categories
    |
    | One-to-Many
    |
transactions

categories
    |
    | One-to-Many
    |
budgets
```

One category can be associated with multiple transactions and budgets.

Each transaction and budget belongs to one category.

## 4. Default Categories

Income:
- Salary
- Freelance
- Business
- Investment
- Other Income

Expense:
- Food & Beverage
- Transportation
- Shopping
- Entertainment
- Health
- Education
- Bills
- Other Expenses

These categories should be inserted manually into MySQL during database setup.

## 5. Database Integrity

- Use foreign keys.
- Prevent deletion of categories that are referenced by transactions or budgets.
- Do not use cascading deletion for financial records.
- Use appropriate indexes for foreign keys and transaction dates.
- Validate financial values before database operations.

## 6. Financial Calculation Rules

**Total Income**

Sum all transactions belonging to INCOME categories.

**Total Expenses**

Sum all transactions belonging to EXPENSE categories.

**Total Balance**

Total Income minus Total Expenses.

**Budget Usage**

Compare the budget amount with actual expenses from the same category and selected month/year.

Use transaction_date to determine the relevant month and year.

## 7. JDBC Connection

The Java application connects to the existing MySQL database.

Example connection URL:

```text
jdbc:mysql://localhost:3306/finora_db
```

Use `DatabaseConnection.java` to manage the connection.

Use PreparedStatement for database queries.

Use try-with-resources to close connections, statements, and result sets properly.

## 8. SQL Documentation

A SQL initialization script may be provided as:

`database/finora_db.sql`

The script is intended for manual execution in MySQL Workbench.

It should contain:
- Database creation.
- Three table definitions.
- Foreign key constraints.
- Unique constraints.
- Default category inserts.

The Java application must not execute this script automatically.

## 9. Monetary Data

Use:
- MySQL DECIMAL(15,2).
- Java BigDecimal.

Never use double or float for financial calculations.

## 10. Database Scope

Do not add additional tables in version 1.

Future database extensions require explicit approval.