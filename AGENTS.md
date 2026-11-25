# AGENTS.md — Finora

## Project Overview

Finora is a desktop-based Personal Finance Manager built with JavaFX and MySQL.

The application allows a single local user to manage income, expenses, monthly budgets, and financial reports.

Read the relevant documentation in the `docs/` directory before implementing features.

## Technology Stack

- Java 21
- JavaFX
- FXML
- JavaFX CSS
- MySQL 8
- JDBC
- Maven
- IntelliJ IDEA

## Architecture

Use a simple and understandable structure:

- `config`: Database connection.
- `model`: Data classes.
- `dao`: Database queries and CRUD operations.
- `controller`: UI interactions and application logic.
- `util`: Reusable helper functions.
- `resources`: FXML, CSS, and images.

Do not introduce additional architectural layers unless they are genuinely necessary.

Do not use Spring Boot, Hibernate, JPA, or other unnecessary frameworks.

## Database Rules

IMPORTANT: The database is managed separately using MySQL Workbench or MySQL tools.

- Database name: `finora_db`.
- Main tables: `categories`, `transactions`, and `budgets`.
- Database and tables are created manually by the developer.
- Java connects to the existing database using JDBC.
- The application must not automatically create or modify the database schema.
- Do not introduce additional tables without approval.
- Use PreparedStatement for SQL queries.
- Use try-with-resources for database resources.
- Use BigDecimal for monetary values.
- Do not use double or float for financial calculations.
- Use foreign keys to maintain data integrity.

The SQL file, if provided, is documentation or a manual setup script, not an automatic application migration system.

## UI/UX Rules

- Use a modern, professional fintech dashboard design.
- Default theme: Light Mode.
- Primary color: #16865B.
- Background: #F8FAFC.
- Card background: #FFFFFF.
- Main text: #152B25.
- Secondary text: #64748B.
- Use consistent spacing and typography.
- Use proper icons instead of emojis.
- Avoid crowded layouts and excessive decoration.
- All application interface text should use Indonesian.

## Coding Standards

- Follow Java naming conventions.
- Apply basic OOP principles.
- Keep classes focused and understandable.
- Separate SQL queries from UI controllers.
- Validate user input.
- Handle database errors properly.
- Avoid unnecessary abstractions and complicated design patterns.
- Prefer readable code over excessive complexity.

## Development Workflow

1. Inspect the existing project before making changes.
2. Read relevant documentation in `docs/`.
3. Explain the implementation plan before major changes.
4. Implement features incrementally.
5. Test each feature before continuing.
6. Run Maven compilation after significant changes.
7. Fix errors before moving to the next phase.
8. Update documentation when necessary.

## Important Restrictions

- Do not add login or multi-user functionality.
- Do not create a separate backend server.
- Do not introduce unnecessary frameworks.
- Do not automatically create the MySQL database.
- Do not change the agreed database structure without approval.
- Do not add features outside the project scope without approval.
- Do not create decorative buttons without functionality.
- Do not replace working code unnecessarily.

## Documentation

Follow these documents:

- `docs/PROJECT_OVERVIEW.md`
- `docs/DATABASE_DESIGN.md`
- `docs/UI_UX_GUIDELINES.md`
- `docs/DEVELOPMENT_ROADMAP.md`

If requirements are unclear or conflict with these instructions, explain the issue and ask before making major changes.