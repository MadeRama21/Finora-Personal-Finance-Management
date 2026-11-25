package com.finora.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DatabaseConnection {

    private static final String DB_URL = System.getenv().getOrDefault(
            "FINORA_DB_URL", "jdbc:mysql://localhost:3306/finora_db");
    private static final String DB_USER = System.getenv().getOrDefault("FINORA_DB_USER", "root");
    private static final String DB_PASSWORD = System.getenv().getOrDefault("FINORA_DB_PASSWORD", "");

    private DatabaseConnection() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }
}
