package com.finora.dao;

import com.finora.config.DatabaseConnection;
import com.finora.model.Budget;
import com.finora.model.BudgetSummary;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

public class BudgetDao {

    public List<BudgetSummary> findAll(int month, int year) throws SQLException {
        YearMonth period = YearMonth.of(year, month);
        LocalDate firstDay = period.atDay(1);
        LocalDate lastDay = period.atEndOfMonth();

        String sql = "SELECT b.id, b.category_id, b.amount, b.month, b.year, "
                + "c.name AS category_name, COALESCE(SUM(t.amount), 0) AS spent "
                + "FROM budgets b "
                + "JOIN categories c ON c.id = b.category_id "
                + "LEFT JOIN transactions t ON t.category_id = b.category_id "
                + "AND t.transaction_date BETWEEN ? AND ? "
                + "WHERE b.month = ? AND b.year = ? "
                + "GROUP BY b.id, b.category_id, b.amount, b.month, b.year, c.name "
                + "ORDER BY c.name";

        List<BudgetSummary> summaries = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDate(1, java.sql.Date.valueOf(firstDay));
            statement.setDate(2, java.sql.Date.valueOf(lastDay));
            statement.setInt(3, month);
            statement.setInt(4, year);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Budget budget = new Budget(
                            resultSet.getInt("id"),
                            resultSet.getInt("category_id"),
                            resultSet.getBigDecimal("amount"),
                            resultSet.getInt("month"),
                            resultSet.getInt("year")
                    );
                    summaries.add(new BudgetSummary(
                            budget,
                            resultSet.getString("category_name"),
                            resultSet.getBigDecimal("spent")
                    ));
                }
            }
        }
        return summaries;
    }

    public boolean existsDuplicate(int categoryId, int month, int year, int excludeId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM budgets "
                + "WHERE category_id = ? AND month = ? AND year = ? AND id <> ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, categoryId);
            statement.setInt(2, month);
            statement.setInt(3, year);
            statement.setInt(4, excludeId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) > 0;
            }
        }
    }

    public int insert(Budget budget) throws SQLException {
        String sql = "INSERT INTO budgets (category_id, amount, month, year) VALUES (?, ?, ?, ?)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, budget.getCategoryId());
            statement.setBigDecimal(2, budget.getAmount());
            statement.setInt(3, budget.getMonth());
            statement.setInt(4, budget.getYear());
            return statement.executeUpdate();
        }
    }

    public int update(Budget budget) throws SQLException {
        String sql = "UPDATE budgets SET category_id = ?, amount = ?, month = ?, year = ? WHERE id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, budget.getCategoryId());
            statement.setBigDecimal(2, budget.getAmount());
            statement.setInt(3, budget.getMonth());
            statement.setInt(4, budget.getYear());
            statement.setInt(5, budget.getId());
            return statement.executeUpdate();
        }
    }

    public int delete(int budgetId) throws SQLException {
        String sql = "DELETE FROM budgets WHERE id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, budgetId);
            return statement.executeUpdate();
        }
    }
}
