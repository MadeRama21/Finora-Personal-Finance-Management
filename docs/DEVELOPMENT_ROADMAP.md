# Finora — Development Roadmap

## Development Approach

Develop Finora incrementally.

Complete and test each phase before moving to the next.

Avoid implementing unnecessary features or complicated architecture.

## Phase 1 — Project Setup

Tasks:
- Check Java 21 installation.
- Create Maven project.
- Configure JavaFX dependencies.
- Configure JavaFX Maven plugin.
- Create the initial JavaFX application.
- Verify the application launches.

Deliverables:
- Working Maven project.
- JavaFX application window.
- Basic project structure.

Acceptance criteria:
- `mvn clean compile` succeeds.
- `mvn javafx:run` launches the application.

## Phase 2 — MySQL Database Setup

This phase is performed separately from the Java application.

Tasks:
- Open MySQL Workbench.
- Create database `finora_db`.
- Create `categories` table.
- Create `transactions` table.
- Create `budgets` table.
- Create foreign key and unique constraints.
- Insert default categories.

After creating the database:

- Add MySQL Connector/J dependency to Maven.
- Create `DatabaseConnection.java`.
- Configure JDBC connection.
- Test the connection from Java.

Deliverables:
- Working MySQL database.
- Three database tables.
- Working JDBC connection.

Acceptance criteria:
- Database and tables exist in MySQL.
- Java successfully connects to the existing database.
- No automatic database creation is implemented in the application.

## Phase 3 — Basic Application Structure

Tasks:
- Create model classes.
- Create DAO classes.
- Create controller classes.
- Create the main application layout.
- Configure FXML and CSS.
- Implement sidebar navigation.

Deliverables:
- Basic application structure.
- Working main layout.
- Working navigation.

Acceptance criteria:
- The application opens successfully.
- Main navigation works.
- Code follows the agreed simple architecture.

## Phase 4 — Main Features

Implement features in this order.

### 4.1 Categories

- Display categories.
- Add categories.
- Edit categories.
- Delete categories safely.

### 4.2 Transactions

- Display transactions.
- Add income transactions.
- Add expense transactions.
- Edit transactions.
- Delete transactions.
- Search and filter transactions.

### 4.3 Dashboard

- Display total balance.
- Display total income.
- Display total expenses.
- Display recent transactions.
- Display financial charts.

### 4.4 Budgets

- Create monthly budgets.
- Edit budgets.
- Delete budgets.
  - Calculate actual spending.
- Calculate remaining budget.
- Display budget progress.

### 4.5 Reports

- Display financial summaries.
- Display income versus expenses.
- Display expenses by category.
- Compare monthly financial activities.

Acceptance criteria:
- All features use actual MySQL records.
- CRUD operations work correctly.
- Financial calculations are correct.

## Phase 5 — Testing and Refinement

Tasks:
- Test database connectivity.
- Test CRUD operations.
- Test financial calculations.
- Test input validation.
- Test date filters.
- Test empty states.
- Test navigation.
- Fix bugs.
- Improve UI consistency.

Acceptance criteria:
- No known critical errors.
- Database operations work correctly.
- Data remains available after restarting the application.
- UI is ready for demonstration.

## Phase 6 — Finalization

Tasks:
- Prepare sample data.
- Update README.
- Complete project documentation.
- Prepare application screenshots.
- Prepare project demonstration.

Deliverables:
- Working Finora application.
- Database setup instructions.
- Project documentation.
- Presentation-ready application.

## Development Status

| Phase | Status |
|---|---|
| Phase 1 — Project Setup | Completed |
| Phase 2 — MySQL Database Setup | Completed |
| Phase 3 — Basic Application Structure | Completed |
| Phase 4 — Main Features | Completed (4.1–4.5 done; budget month uniqueness verified in `finora_db`) |
| Phase 5 — Testing and Refinement | Completed (regression + validation/date-range refinements) |
| Phase 6 — Finalization | Completed (manual SQL scripts, README, demo guide, screenshots) |

> Note: The MySQL database and its tables are created and managed manually in MySQL Workbench.
> The Java application only connects to the existing `finora_db` through JDBC.
> Manual setup, constraint-fix, and sample-data scripts live in `database/` and are never run by the app.

## Development Rules

1. Work on one phase at a time.
2. Test each phase before continuing.
3. Do not skip important acceptance criteria.
4. Keep the implementation simple.
5. Do not introduce unnecessary dependencies.
6. Do not modify the database structure without approval.
7. Ask before adding features outside the agreed scope.