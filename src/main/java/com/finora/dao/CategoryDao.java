package com.finora.dao;

import com.finora.config.DatabaseConnection;
import com.finora.model.Category;
import com.finora.model.CategoryType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CategoryDao {

    public List<Category> findAll() throws SQLException {
        String sql = "SELECT id, name, type, icon FROM categories ORDER BY type, name";
        List<Category> categories = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                categories.add(new Category(
                        resultSet.getInt("id"),
                        resultSet.getString("name"),
                        CategoryType.valueOf(resultSet.getString("type")),
                        resultSet.getString("icon")
                ));
            }
        }
        return categories;
    }

    public int insert(Category category) throws SQLException {
        String sql = "INSERT INTO categories (name, type, icon) VALUES (?, ?, ?)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, category.getName());
            statement.setString(2, category.getType().name());
            statement.setString(3, category.getIcon());
            return statement.executeUpdate();
        }
    }

    public int update(Category category) throws SQLException {
        String sql = "UPDATE categories SET name = ?, type = ?, icon = ? WHERE id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, category.getName());
            statement.setString(2, category.getType().name());
            statement.setString(3, category.getIcon());
            statement.setInt(4, category.getId());
            return statement.executeUpdate();
        }
    }

    public boolean isReferenced(int categoryId) throws SQLException {
        String sql = "SELECT EXISTS(SELECT 1 FROM transactions WHERE category_id = ?) "
                + "OR EXISTS(SELECT 1 FROM budgets WHERE category_id = ?) AS referenced";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, categoryId);
            statement.setInt(2, categoryId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getInt("referenced") > 0;
            }
        }
    }

    public int delete(int categoryId) throws SQLException {
        String sql = "DELETE FROM categories WHERE id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, categoryId);
            return statement.executeUpdate();
        }
    }
}
