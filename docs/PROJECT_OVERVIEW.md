# Finora — Project Overview

## 1. Project Information

| Property | Description |
|---|---|
| Project Name | Finora |
| Project Type | Desktop Application |
| Application Category | Personal Finance Management |
| Primary Language | Java |
| UI Framework | JavaFX |
| Database | MySQL |
| IDE | IntelliJ IDEA |
| Target Platform | Desktop |

## 2. Project Background

Managing personal finances requires individuals to understand their income, expenses, and spending habits.

Finora is designed to simplify this process by providing a centralized application for recording financial transactions, managing budgets, and reviewing financial reports.

The application focuses on simplicity, usability, and clear financial information.

## 3. Project Objectives

The main objectives are:

1. Provide a centralized platform for managing personal finances.
2. Allow users to record income and expenses.
3. Help users control monthly spending through budgets.
4. Provide financial summaries and visual reports.
5. Demonstrate Java OOP, database integration, and desktop UI development.

## 4. Target Users

Individuals who want to:
- Track their daily income and expenses.
- Understand their spending patterns.
- Manage monthly budgets.
- Monitor their financial condition.

## 5. Application Scope

Finora version 1 is designed for a single local user.

The application does not require:
- Online accounts.
- Cloud synchronization.
- Multi-user authentication.
- Online payment integration.
- Banking API integration.

All financial data is stored in a local MySQL database.

## 6. Core Features

### 6.1 Dashboard

Purpose: Display an overview of the user's financial condition.

Requirements:
- Display total balance.
- Display total income.
- Display total expenses.
- Display income versus expense chart.
- Display expense distribution by category.
- Display recent transactions.
- Support relevant date filters.

All financial values must be calculated from database records.

### 6.2 Transactions

Purpose: Manage financial records.

Requirements:
- Add income transaction.
- Add expense transaction.
- Edit transaction.
- Delete transaction.
- Display transaction history.
- Search transactions.
- Filter by category.
- Filter by transaction type.
- Filter by date range.

Transaction fields:
- Transaction type.
- Category.
- Amount.
- Description.
- Transaction date.

### 6.3 Budgets

Purpose: Control monthly spending.

Requirements:
- Create monthly budget.
- Edit budget.
- Delete budget.
- Display spending progress.
- Calculate remaining budget.
- Display budget utilization percentage.
- Filter by month and year.

Budgets are assigned to expense categories only.

### 6.4 Categories

Purpose: Organize financial transactions.

Requirements:
- Add category.
- Edit category.
- Delete category when safe.
- Display category list.
- Assign category type.

Category types:
- Pemasukan
- Pengeluaran

Default income categories:
- Gaji
- Freelance
- Bisnis
- Investasi
- Pemasukan Lainnya

Default expense categories:
- Makanan & Minuman
- Transportasi
- Belanja
- Hiburan
- Kesehatan
- Pendidikan
- Tagihan
- Pengeluaran Lainnya

### 6.5 Reports

Purpose: Analyze financial activities.

Requirements:
- Display total income.
- Display total expenses.
- Display net balance for the selected period.
- Display monthly financial comparison.
- Display expense distribution by category.
- Support date-based filtering.

### 6.6 Settings

Purpose: Manage basic application preferences.

Requirements:
- Display application information.
- Configure currency preference.
- Manage basic application preferences.

Default currency: IDR (Indonesian Rupiah).

## 7. Business Rules

1. Every transaction must belong to an existing category.
2. Income transactions must use INCOME categories.
3. Expense transactions must use EXPENSE categories.
4. Transaction amounts must be greater than zero.
5. Budget amounts must be greater than zero.
6. Budgets can only be assigned to expense categories.
7. A category cannot be deleted while it is referenced by transactions or budgets.
8. Financial calculations must use BigDecimal.
9. Dashboard values must be calculated from database records.
10. Deleting a transaction must update related financial summaries automatically.

## 8. Financial Calculation Rules

### Total Income

Sum all income transactions within the selected period.

### Total Expenses

Sum all expense transactions within the selected period.

### Net Balance

Total Income minus Total Expenses.

### Budget Remaining

Budget Amount minus Actual Expenses.

### Budget Utilization

Actual Expenses divided by Budget Amount, multiplied by 100.

All calculations must handle empty records and zero budget amounts safely.

## 9. Non-Functional Requirements

- The application must be easy to use.
- The UI must be consistent across pages.
- Database operations must be reliable.
- SQL queries must use PreparedStatement.
- Financial calculations must maintain decimal precision.
- The application must handle invalid inputs safely.
- Database operations should not freeze the JavaFX UI.

## 10. Out of Scope

The following features are not included in version 1:

- Multi-user authentication.
- Bank account integration.
- Automatic transaction synchronization.
- Online payment processing.
- Cloud backup.
- Investment portfolio tracking.
- Advanced financial forecasting.

These features may be considered in future versions.

## 11. Implementation Status (v1.0)

All six development phases are complete. The delivered application implements
sections 6.1–6.5 above. Section 6.6 (Settings) is intentionally not included in
the v1 navigation: currency, theme, and preferences are fixed by the UI guidelines,
and adding a non-functional page would violate the no-decorative-controls rule.

Additional delivery notes:
- Savings rate and transaction count are included in Reports alongside the
  summaries required in section 6.5.
- Manual SQL scripts in `database/` cover fresh setup, the budget month-uniqueness
  constraint, and optional demo data. The application never executes them.
- `docs/DEMO_GUIDE.md` provides a step-by-step presentation walkthrough.
- Screenshots are stored in `../screenshots`.