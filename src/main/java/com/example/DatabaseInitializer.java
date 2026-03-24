package com.example;

import java.sql.*;
import java.time.Instant;

public class DatabaseInitializer {
    
    public static void initialize() {
        createUserTable();
        createAuditLogTable();
        createPortfolioTable();
        createHoldingTable();
        createPaymentTable();
        
        initializeDefaultUsers();
    }

    private static void createUserTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS USER (
                user_id TEXT PRIMARY KEY,
                username TEXT UNIQUE NOT NULL,
                password_hash TEXT NOT NULL,
                role TEXT NOT NULL,
                active INTEGER DEFAULT 1,
                created_at TIMESTAMP,
                updated_at TIMESTAMP
            )
            """;

        executeStatement(sql, "USER");
    }

    private static void createAuditLogTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS AUDIT_LOG (
                event_id TEXT PRIMARY KEY,
                event_time TIMESTAMP NOT NULL,
                actor_id TEXT NOT NULL,
                actor_role TEXT,
                action TEXT NOT NULL,
                target_type TEXT NOT NULL,
                target_id TEXT,
                result TEXT NOT NULL,
                error_code TEXT,
                request_id TEXT,
                source_ip TEXT
            )
            """;

        executeStatement(sql, "AUDIT_LOG");
    }

    private static void createPortfolioTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS PORTFOLIO (
                portfolio_id TEXT PRIMARY KEY,
                user_id TEXT NOT NULL,
                name TEXT NOT NULL,
                created_at TIMESTAMP,
                updated_at TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES USER(user_id)
            )
            """;

        executeStatement(sql, "PORTFOLIO");
    }

    private static void createHoldingTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS HOLDING (
                holding_id TEXT PRIMARY KEY,
                portfolio_id TEXT NOT NULL,
                asset_symbol TEXT NOT NULL,
                quantity REAL NOT NULL,
                avg_price REAL NOT NULL,
                created_at TIMESTAMP,
                updated_at TIMESTAMP,
                FOREIGN KEY (portfolio_id) REFERENCES PORTFOLIO(portfolio_id)
            )
            """;

        executeStatement(sql, "HOLDING");
    }

    private static void createPaymentTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS PAYMENT (
                payment_id TEXT PRIMARY KEY,
                portfolio_id TEXT NOT NULL,
                amount REAL NOT NULL,
                payment_type TEXT NOT NULL,
                status TEXT NOT NULL,
                created_at TIMESTAMP,
                updated_at TIMESTAMP,
                FOREIGN KEY (portfolio_id) REFERENCES PORTFOLIO(portfolio_id)
            )
            """;

        executeStatement(sql, "PAYMENT");
    }

    private static void executeStatement(String sql, String tableName) {
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(sql);
            conn.commit();
            System.out.println("Table " + tableName + " initialized");

        } catch (SQLException e) {
            System.err.println("Error creating " + tableName + " table: " + e.getMessage());
        }
    }

    private static void initializeDefaultUsers() {
        AuthService authService = new AuthService();
        
        if (authService.findUserByUsername("admin").isEmpty()) {
            User admin = new User("admin-001", "admin", "", Role.ADMIN);
            admin.setCreatedAt(Instant.now());
            admin.setUpdatedAt(Instant.now());
            authService.createUser(admin, "admin123");
            System.out.println("Created default admin user (admin/admin123)");
        }
        
        if (authService.findUserByUsername("supervisor").isEmpty()) {
            User supervisor = new User("sup-001", "supervisor", "", Role.SUPERVISOR);
            supervisor.setCreatedAt(Instant.now());
            supervisor.setUpdatedAt(Instant.now());
            authService.createUser(supervisor, "super123");
            System.out.println("Created default supervisor user (supervisor/super123)");
        }
        
        if (authService.findUserByUsername("operator").isEmpty()) {
            User operator = new User("op-001", "operator", "", Role.OPERATOR);
            operator.setCreatedAt(Instant.now());
            operator.setUpdatedAt(Instant.now());
            authService.createUser(operator, "operator123");
            System.out.println("Created default operator user (operator/operator123)");
        }
        
        if (authService.findUserByUsername("viewer").isEmpty()) {
            User viewer = new User("viewer-001", "viewer", "", Role.VIEWER);
            viewer.setCreatedAt(Instant.now());
            viewer.setUpdatedAt(Instant.now());
            authService.createUser(viewer, "viewer123");
            System.out.println("Created default viewer user (viewer/viewer123)");
        }
    }
}
