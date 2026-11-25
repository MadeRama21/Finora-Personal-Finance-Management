package com.finora.dao;

import com.finora.config.DatabaseConnection;
import com.finora.model.CategorySummary;
import com.finora.model.CategoryType;
import com.finora.model.DashboardData;
import com.finora.model.MonthlyTotal;
import com.finora.model.Transaction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TransactionDao {

    public List<Transaction> findAll(String search, CategoryType type, Integer categoryId,
                                     LocalDate from, LocalDate to) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT t.id, t.category_id, t.amount, t.description, t.transaction_date, t.created_at, "
                        + "c.name AS category_name, c.type AS category_type "
                        + "FROM transactions t JOIN categories c ON c.id = t.category_id WHERE 1 = 1 ");

        List<Object> parameters = new ArrayList<>();

        if (type != null) {
            sql.append("AND c.type = ? ");
            parameters.add(type.name());
        }
        if (categoryId != null) {
            sql.append("AND t.category_id = ? ");
            parameters.add(categoryId);
        }
        if (from != null) {
            sql.append("AND t.transaction_date >= ? ");
            parameters.add(java.sql.Date.valueOf(from));
        }
        if (to != null) {
            sql.append("AND t.transaction_date <= ? ");
            parameters.add(java.sql.Date.valueOf(to));
        }
        if (search != null && !search.isBlank()) {
            sql.append("AND (LOWER(COALESCE(t.description, '')) LIKE ? OR LOWER(c.name) LIKE ?) ");
            String pattern = "%" + search.toLowerCase(Locale.ROOT) + "%";
            parameters.add(pattern);
            parameters.add(pattern);
        }
        sql.append("ORDER BY t.transaction_date DESC, t.id DESC");

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {

            setParameters(statement, parameters);
            List<Transaction> transactions = new ArrayList<>();
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    transactions.add(toTransaction(resultSet));
                }
            }
            return transactions;
        }
    }

    public DashboardData getDashboardData(LocalDate from, LocalDate to, int recentLimit) throws SQLException {
        return new DashboardData(
                sumByType("INCOME", from, to),
                sumByType("EXPENSE", from, to),
                expenseByCategory(from, to),
                monthlyTotals(from, to),
                findRecent(recentLimit, from, to)
        );
    }

    public int countTransactions(LocalDate from, LocalDate to) throws SQLException {
        String sql = "SELECT COUNT(*) FROM transactions WHERE transaction_date BETWEEN ? AND ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDate(1, java.sql.Date.valueOf(from));
            statement.setDate(2, java.sql.Date.valueOf(to));

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getInt(1) : 0;
            }
        }
    }

    public int insert(Transaction transaction) throws SQLException {
        String sql = "INSERT INTO transactions (category_id, amount, description, transaction_date) "
                + "VALUES (?, ?, ?, ?)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, transaction.getCategoryId());
            statement.setBigDecimal(2, transaction.getAmount());
            statement.setString(3, transaction.getDescription());
            statement.setDate(4, java.sql.Date.valueOf(transaction.getTransactionDate()));
            return statement.executeUpdate();
        }
    }

    public int update(Transaction transaction) throws SQLException {
        String sql = "UPDATE transactions SET category_id = ?, amount = ?, description = ?, "
                + "transaction_date = ? WHERE id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, transaction.getCategoryId());
            statement.setBigDecimal(2, transaction.getAmount());
            statement.setString(3, transaction.getDescription());
            statement.setDate(4, java.sql.Date.valueOf(transaction.getTransactionDate()));
            statement.setInt(5, transaction.getId());
            return statement.executeUpdate();
        }
    }

    public int delete(int transactionId) throws SQLException {
        String sql = "DELETE FROM transactions WHERE id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, transactionId);
            return statement.executeUpdate();
        }
    }

    private java.math.BigDecimal sumByType(String type, LocalDate from, LocalDate to) throws SQLException {
        String sql = "SELECT COALESCE(SUM(t.amount), 0) AS total "
                + "FROM transactions t JOIN categories c ON c.id = t.category_id "
                + "WHERE c.type = ? AND t.transaction_date BETWEEN ? AND ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, type);
            statement.setDate(2, java.sql.Date.valueOf(from));
            statement.setDate(3, java.sql.Date.valueOf(to));

            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getBigDecimal("total");
            }
        }
    }

    private List<CategorySummary> expenseByCategory(LocalDate from, LocalDate to) throws SQLException {
        String sql = "SELECT c.name AS category_name, SUM(t.amount) AS total "
                + "FROM transactions t JOIN categories c ON c.id = t.category_id "
                + "WHERE c.type = 'EXPENSE' AND t.transaction_date BETWEEN ? AND ? "
                + "GROUP BY c.id, c.name ORDER BY total DESC";

        List<CategorySummary> result = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDate(1, java.sql.Date.valueOf(from));
            statement.setDate(2, java.sql.Date.valueOf(to));

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    result.add(new CategorySummary(
                            resultSet.getString("category_name"),
                            resultSet.getBigDecimal("total")));
                }
            }
        }
        return result;
    }

    private List<MonthlyTotal> monthlyTotals(LocalDate from, LocalDate to) throws SQLException {
        String sql = "SELECT YEAR(t.transaction_date) AS tx_year, MONTH(t.transaction_date) AS tx_month, "
                + "SUM(CASE WHEN c.type = 'INCOME' THEN t.amount ELSE 0 END) AS income, "
                + "SUM(CASE WHEN c.type = 'EXPENSE' THEN t.amount ELSE 0 END) AS expense "
                + "FROM transactions t JOIN categories c ON c.id = t.category_id "
                + "WHERE t.transaction_date BETWEEN ? AND ? "
                + "GROUP BY YEAR(t.transaction_date), MONTH(t.transaction_date) "
                + "ORDER BY tx_year, tx_month";

        List<MonthlyTotal> result = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDate(1, java.sql.Date.valueOf(from));
            statement.setDate(2, java.sql.Date.valueOf(to));

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    result.add(new MonthlyTotal(
                            resultSet.getInt("tx_year"),
                            resultSet.getInt("tx_month"),
                            resultSet.getBigDecimal("income"),
                            resultSet.getBigDecimal("expense")));
                }
            }
        }
        return result;
    }

    private List<Transaction> findRecent(int limit, LocalDate from, LocalDate to) throws SQLException {
        String sql = "SELECT t.id, t.category_id, t.amount, t.description, t.transaction_date, t.created_at, "
                + "c.name AS category_name, c.type AS category_type "
                + "FROM transactions t JOIN categories c ON c.id = t.category_id "
                + "WHERE t.transaction_date BETWEEN ? AND ? "
                + "ORDER BY t.transaction_date DESC, t.id DESC LIMIT ?";

        List<Transaction> transactions = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDate(1, java.sql.Date.valueOf(from));
            statement.setDate(2, java.sql.Date.valueOf(to));
            statement.setInt(3, limit);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    transactions.add(toTransaction(resultSet));
                }
            }
        }
        return transactions;
    }

    private void setParameters(PreparedStatement statement, List<Object> parameters) throws SQLException {
        for (int i = 0; i < parameters.size(); i++) {
            statement.setObject(i + 1, parameters.get(i));
        }
    }

    private Transaction toTransaction(ResultSet resultSet) throws SQLException {
        Timestamp createdAt = resultSet.getTimestamp("created_at");
        return new Transaction(
                resultSet.getInt("id"),
                resultSet.getInt("category_id"),
                resultSet.getBigDecimal("amount"),
                resultSet.getString("description"),
                resultSet.getDate("transaction_date").toLocalDate(),
                createdAt == null ? null : createdAt.toLocalDateTime(),
                resultSet.getString("category_name"),
                CategoryType.valueOf(resultSet.getString("category_type"))
        );
    }
}
