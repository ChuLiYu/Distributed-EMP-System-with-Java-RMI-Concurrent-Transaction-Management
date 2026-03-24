package com.example;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.*;
import java.util.Base64;
import java.util.Optional;

public class AuthService {
    private static final int SALT_LENGTH = 16;
    private static final String DELIMITER = ":";

    public static class AuthResult {
        private final boolean success;
        private final User user;
        private final String errorCode;

        public AuthResult(boolean success, User user, String errorCode) {
            this.success = success;
            this.user = user;
            this.errorCode = errorCode;
        }

        public boolean isSuccess() {
            return success;
        }

        public User getUser() {
            return user;
        }

        public String getErrorCode() {
            return errorCode;
        }
    }

    public AuthResult authenticate(String username, String password) {
        Optional<User> userOpt = findUserByUsername(username);
        
        if (userOpt.isEmpty()) {
            return new AuthResult(false, null, "USER_NOT_FOUND");
        }

        User user = userOpt.get();
        
        if (!user.isActive()) {
            return new AuthResult(false, null, "USER_INACTIVE");
        }

        String storedHash = user.getPasswordHash();
        if (!verifyPassword(password, storedHash)) {
            return new AuthResult(false, null, "INVALID_PASSWORD");
        }

        return new AuthResult(true, user, null);
    }

    public String hashPassword(String password) {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);

        String saltString = Base64.getEncoder().encodeToString(salt);
        String hash = hashWithSalt(password, salt);

        return saltString + DELIMITER + hash;
    }

    private boolean verifyPassword(String password, String storedHash) {
        if (storedHash == null || !storedHash.contains(DELIMITER)) {
            return false;
        }

        String[] parts = storedHash.split(DELIMITER);
        if (parts.length != 2) {
            return false;
        }

        String saltString = parts[0];
        String expectedHash = parts[1];

        byte[] salt = Base64.getDecoder().decode(saltString);
        String actualHash = hashWithSalt(password, salt);

        return expectedHash.equals(actualHash);
    }

    private String hashWithSalt(String password, byte[] salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] hashedPassword = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hashedPassword);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    public Optional<User> findUserByUsername(String username) {
        String sql = "SELECT * FROM USER WHERE username = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToUser(rs));
            }
            
            rs.close();
        } catch (SQLException e) {
            System.err.println("Error finding user: " + e.getMessage());
        }
        
        return Optional.empty();
    }

    public Optional<User> findUserById(String userId) {
        String sql = "SELECT * FROM USER WHERE user_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToUser(rs));
            }
            
            rs.close();
        } catch (SQLException e) {
            System.err.println("Error finding user: " + e.getMessage());
        }
        
        return Optional.empty();
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getString("user_id"));
        user.setUsername(rs.getString("username"));
        user.setPasswordHash(rs.getString("password_hash"));
        
        String roleStr = rs.getString("role");
        user.setRole(Role.valueOf(roleStr));
        
        user.setActive(rs.getBoolean("active"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            user.setCreatedAt(createdAt.toInstant());
        }
        
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            user.setUpdatedAt(updatedAt.toInstant());
        }
        
        return user;
    }

    public boolean createUser(User user, String plainPassword) {
        String sql = "INSERT INTO USER (user_id, username, password_hash, role, active, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            String hashedPassword = hashPassword(plainPassword);
            
            pstmt.setString(1, user.getUserId());
            pstmt.setString(2, user.getUsername());
            pstmt.setString(3, hashedPassword);
            pstmt.setString(4, user.getRole().name());
            pstmt.setBoolean(5, user.isActive());
            pstmt.setTimestamp(6, Timestamp.from(user.getCreatedAt()));
            pstmt.setTimestamp(7, Timestamp.from(user.getUpdatedAt()));
            
            int result = pstmt.executeUpdate();
            conn.commit();
            
            return result > 0;
            
        } catch (SQLException e) {
            System.err.println("Error creating user: " + e.getMessage());
            return false;
        }
    }

    public void initDefaultUsers() {
        if (findUserByUsername("admin").isEmpty()) {
            User admin = new User("admin-001", "admin", "", Role.ADMIN);
            createUser(admin, "admin123");
            System.out.println("Created default admin user");
        }
        
        if (findUserByUsername("supervisor").isEmpty()) {
            User supervisor = new User("sup-001", "supervisor", "", Role.SUPERVISOR);
            createUser(supervisor, "super123");
            System.out.println("Created default supervisor user");
        }
        
        if (findUserByUsername("operator").isEmpty()) {
            User operator = new User("op-001", "operator", "", Role.OPERATOR);
            createUser(operator, "operator123");
            System.out.println("Created default operator user");
        }
        
        if (findUserByUsername("viewer").isEmpty()) {
            User viewer = new User("viewer-001", "viewer", "", Role.VIEWER);
            createUser(viewer, "viewer123");
            System.out.println("Created default viewer user");
        }
    }
}
