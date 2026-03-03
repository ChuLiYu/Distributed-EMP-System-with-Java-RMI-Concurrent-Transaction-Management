package com.example;

/**
 * Provides the Connection to the database
 */
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;

public class DBConnection {
    public static Connection getConnection() throws SQLException {
        String dbURL = System.getProperty("emp.db.url");
        if (dbURL == null || dbURL.isBlank()) {
            dbURL = System.getenv("EMP_DB_URL");
        }

        if (dbURL == null || dbURL.isBlank()) {
            dbURL = "jdbc:sqlite:" + resolveDefaultDbPath().toString();
        }
        Connection conn = DriverManager.getConnection(dbURL);
        conn.setAutoCommit(false);

        return conn;
    }

    private static Path resolveDefaultDbPath() {
        String dbName = "CSCI7785_database.db";
        Path[] candidates = new Path[] {
                Path.of(dbName),
                Path.of("..", dbName),
                Path.of("demo", dbName)
        };

        for (Path candidate : candidates) {
            Path normalized = candidate.toAbsolutePath().normalize();
            if (Files.exists(normalized)) {
                return normalized;
            }
        }

        return Path.of(dbName).toAbsolutePath().normalize();
    }
}
