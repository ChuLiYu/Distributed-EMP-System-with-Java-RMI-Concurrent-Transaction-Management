package com.example;

import java.sql.*;
import java.time.Instant;
import java.util.*;

public class PortfolioService {
    private final AuthService authService;
    private final AuditService auditService;

    public PortfolioService() {
        this.authService = new AuthService();
        this.auditService = AuditService.getInstance();
    }

    public static class ServiceResult<T> {
        private final boolean success;
        private final T data;
        private final String errorCode;
        private final String requestId;

        public ServiceResult(boolean success, T data, String errorCode, String requestId) {
            this.success = success;
            this.data = data;
            this.errorCode = errorCode;
            this.requestId = requestId;
        }

        public boolean isSuccess() {
            return success;
        }

        public T getData() {
            return data;
        }

        public String getErrorCode() {
            return errorCode;
        }

        public String getRequestId() {
            return requestId;
        }
    }

    public ServiceResult<String> login(String username, String password, String requestId) {
        AuthService.AuthResult authResult = authService.authenticate(username, password);
        
        if (authResult.isSuccess()) {
            auditService.logEvent(AuditEvent.createLoginEvent(
                authResult.getUser().getUserId(),
                authResult.getUser().getRole().name(),
                requestId
            ));
            return new ServiceResult<>(true, authResult.getUser().getUserId(), null, requestId);
        } else {
            auditService.logEvent(AuditEvent.createLoginFailedEvent(username, requestId, authResult.getErrorCode()));
            return new ServiceResult<>(false, null, authResult.getErrorCode(), requestId);
        }
    }

    public ServiceResult<List<Portfolio>> getPortfolios(String userId, String role, String requestId) {
        if (!hasPermission(role, Permission.READ)) {
            auditService.logEvent(AuditEvent.createAccessDeniedEvent(userId, role, "READ", "PORTFOLIO", requestId));
            return new ServiceResult<>(false, null, "ACCESS_DENIED", requestId);
        }

        List<Portfolio> portfolios = findAllPortfolios();
        
        auditService.logEvent(AuditEvent.createDataEvent(
            userId, role, AuditEvent.ActionType.READ, "PORTFOLIO", "ALL", 
            AuditEvent.ResultType.SUCCESS, requestId
        ));
        
        return new ServiceResult<>(true, portfolios, null, requestId);
    }

    public ServiceResult<Portfolio> createPortfolio(String userId, String role, String requestId, String portfolioId, String name) {
        if (!hasPermission(role, Permission.CREATE)) {
            auditService.logEvent(AuditEvent.createAccessDeniedEvent(userId, role, "CREATE", "PORTFOLIO", requestId));
            return new ServiceResult<>(false, null, "ACCESS_DENIED", requestId);
        }

        ValidationUtils.ValidationResult validation = ValidationUtils.validatePortfolioId(portfolioId);
        if (!validation.isValid()) {
            return new ServiceResult<>(false, null, "INVALID_INPUT:" + validation.getErrorMessage(), requestId);
        }

        validation = ValidationUtils.validateName(name);
        if (!validation.isValid()) {
            return new ServiceResult<>(false, null, "INVALID_INPUT:" + validation.getErrorMessage(), requestId);
        }

        Portfolio portfolio = new Portfolio(portfolioId, userId, name);
        
        boolean success = insertPortfolio(portfolio);
        
        if (success) {
            auditService.logEvent(AuditEvent.createDataEvent(
                userId, role, AuditEvent.ActionType.CREATE, "PORTFOLIO", portfolioId,
                AuditEvent.ResultType.SUCCESS, requestId
            ));
            return new ServiceResult<>(true, portfolio, null, requestId);
        } else {
            auditService.logEvent(AuditEvent.createDataEvent(
                userId, role, AuditEvent.ActionType.CREATE, "PORTFOLIO", portfolioId,
                AuditEvent.ResultType.FAIL, requestId
            ));
            return new ServiceResult<>(false, null, "CREATE_FAILED", requestId);
        }
    }

    public ServiceResult<Portfolio> getPortfolio(String userId, String role, String requestId, String portfolioId) {
        if (!hasPermission(role, Permission.READ)) {
            auditService.logEvent(AuditEvent.createAccessDeniedEvent(userId, role, "READ", "PORTFOLIO", requestId));
            return new ServiceResult<>(false, null, "ACCESS_DENIED", requestId);
        }

        Portfolio portfolio = findPortfolioById(portfolioId);
        
        if (portfolio == null) {
            return new ServiceResult<>(false, null, "NOT_FOUND", requestId);
        }

        auditService.logEvent(AuditEvent.createDataEvent(
            userId, role, AuditEvent.ActionType.READ, "PORTFOLIO", portfolioId,
            AuditEvent.ResultType.SUCCESS, requestId
        ));
        
        return new ServiceResult<>(true, portfolio, null, requestId);
    }

    public ServiceResult<Portfolio> updatePortfolio(String userId, String role, String requestId, String portfolioId, String name) {
        if (!hasPermission(role, Permission.UPDATE)) {
            auditService.logEvent(AuditEvent.createAccessDeniedEvent(userId, role, "UPDATE", "PORTFOLIO", requestId));
            return new ServiceResult<>(false, null, "ACCESS_DENIED", requestId);
        }

        ValidationUtils.ValidationResult validation = ValidationUtils.validateName(name);
        if (!validation.isValid()) {
            return new ServiceResult<>(false, null, "INVALID_INPUT:" + validation.getErrorMessage(), requestId);
        }

        Portfolio existing = findPortfolioById(portfolioId);
        if (existing == null) {
            return new ServiceResult<>(false, null, "NOT_FOUND", requestId);
        }

        existing.setName(name);
        existing.setUpdatedAt(Instant.now());
        
        boolean success = updatePortfolio(existing);
        
        if (success) {
            auditService.logEvent(AuditEvent.createDataEvent(
                userId, role, AuditEvent.ActionType.UPDATE, "PORTFOLIO", portfolioId,
                AuditEvent.ResultType.SUCCESS, requestId
            ));
            return new ServiceResult<>(true, existing, null, requestId);
        } else {
            auditService.logEvent(AuditEvent.createDataEvent(
                userId, role, AuditEvent.ActionType.UPDATE, "PORTFOLIO", portfolioId,
                AuditEvent.ResultType.FAIL, requestId
            ));
            return new ServiceResult<>(false, null, "UPDATE_FAILED", requestId);
        }
    }

    public ServiceResult<Boolean> deletePortfolio(String userId, String role, String requestId, String portfolioId) {
        if (!hasPermission(role, Permission.DELETE)) {
            auditService.logEvent(AuditEvent.createAccessDeniedEvent(userId, role, "DELETE", "PORTFOLIO", requestId));
            return new ServiceResult<>(false, null, "ACCESS_DENIED", requestId);
        }

        Portfolio existing = findPortfolioById(portfolioId);
        if (existing == null) {
            return new ServiceResult<>(false, null, "NOT_FOUND", requestId);
        }

        boolean success = deletePortfolio(portfolioId);
        
        if (success) {
            auditService.logEvent(AuditEvent.createDataEvent(
                userId, role, AuditEvent.ActionType.DELETE, "PORTFOLIO", portfolioId,
                AuditEvent.ResultType.SUCCESS, requestId
            ));
            return new ServiceResult<>(true, true, null, requestId);
        } else {
            auditService.logEvent(AuditEvent.createDataEvent(
                userId, role, AuditEvent.ActionType.DELETE, "PORTFOLIO", portfolioId,
                AuditEvent.ResultType.FAIL, requestId
            ));
            return new ServiceResult<>(false, null, "DELETE_FAILED", requestId);
        }
    }

    public ServiceResult<Holding> addHolding(String userId, String role, String requestId, String holdingId, String portfolioId, String assetSymbol, double quantity, double avgPrice) {
        if (!hasPermission(role, Permission.CREATE)) {
            auditService.logEvent(AuditEvent.createAccessDeniedEvent(userId, role, "CREATE", "HOLDING", requestId));
            return new ServiceResult<>(false, null, "ACCESS_DENIED", requestId);
        }

        ValidationUtils.ValidationResult validation = ValidationUtils.validateAssetSymbol(assetSymbol);
        if (!validation.isValid()) {
            return new ServiceResult<>(false, null, "INVALID_INPUT:" + validation.getErrorMessage(), requestId);
        }

        validation = ValidationUtils.validateQuantity(quantity);
        if (!validation.isValid()) {
            return new ServiceResult<>(false, null, "INVALID_INPUT:" + validation.getErrorMessage(), requestId);
        }

        Portfolio portfolio = findPortfolioById(portfolioId);
        if (portfolio == null) {
            return new ServiceResult<>(false, null, "PORTFOLIO_NOT_FOUND", requestId);
        }

        Holding holding = new Holding(holdingId, portfolioId, assetSymbol, quantity, avgPrice);
        
        boolean success = insertHolding(holding);
        
        if (success) {
            auditService.logEvent(AuditEvent.createDataEvent(
                userId, role, AuditEvent.ActionType.CREATE, "HOLDING", holdingId,
                AuditEvent.ResultType.SUCCESS, requestId
            ));
            return new ServiceResult<>(true, holding, null, requestId);
        } else {
            return new ServiceResult<>(false, null, "CREATE_FAILED", requestId);
        }
    }

    public ServiceResult<List<Holding>> getHoldings(String userId, String role, String requestId, String portfolioId) {
        if (!hasPermission(role, Permission.READ)) {
            return new ServiceResult<>(false, null, "ACCESS_DENIED", requestId);
        }

        List<Holding> holdings = findHoldingsByPortfolio(portfolioId);
        return new ServiceResult<>(true, holdings, null, requestId);
    }

    public ServiceResult<Payment> createPayment(String userId, String role, String requestId, String paymentId, String portfolioId, double amount, Payment.PaymentType paymentType) {
        if (!hasPermission(role, Permission.CREATE)) {
            auditService.logEvent(AuditEvent.createAccessDeniedEvent(userId, role, "CREATE", "PAYMENT", requestId));
            return new ServiceResult<>(false, null, "ACCESS_DENIED", requestId);
        }

        ValidationUtils.ValidationResult validation = ValidationUtils.validateAmount(amount);
        if (!validation.isValid()) {
            return new ServiceResult<>(false, null, "INVALID_INPUT:" + validation.getErrorMessage(), requestId);
        }

        Portfolio portfolio = findPortfolioById(portfolioId);
        if (portfolio == null) {
            return new ServiceResult<>(false, null, "PORTFOLIO_NOT_FOUND", requestId);
        }

        Payment payment = new Payment(paymentId, portfolioId, amount, paymentType);
        
        boolean success = insertPayment(payment);
        
        if (success) {
            auditService.logEvent(AuditEvent.createDataEvent(
                userId, role, AuditEvent.ActionType.CREATE, "PAYMENT", paymentId,
                AuditEvent.ResultType.SUCCESS, requestId
            ));
            return new ServiceResult<>(true, payment, null, requestId);
        } else {
            return new ServiceResult<>(false, null, "CREATE_FAILED", requestId);
        }
    }

    public ServiceResult<List<Payment>> getPayments(String userId, String role, String requestId, String portfolioId) {
        if (!hasPermission(role, Permission.READ)) {
            return new ServiceResult<>(false, null, "ACCESS_DENIED", requestId);
        }

        List<Payment> payments = findPaymentsByPortfolio(portfolioId);
        return new ServiceResult<>(true, payments, null, requestId);
    }

    public ServiceResult<List<AuditEvent>> getAuditLogs(String userId, String role, String requestId, String targetType, String targetId) {
        if (!hasPermission(role, Permission.AUDIT)) {
            auditService.logEvent(AuditEvent.createAccessDeniedEvent(userId, role, "READ", "AUDIT_LOG", requestId));
            return new ServiceResult<>(false, null, "ACCESS_DENIED", requestId);
        }

        List<AuditEvent> logs;
        if (targetType != null && targetId != null) {
            logs = auditService.getAuditLogs(targetType, targetId, 100);
        } else if (targetType != null) {
            logs = auditService.getAuditLogs(targetType, "%", 100);
        } else {
            logs = new ArrayList<>();
        }
        
        return new ServiceResult<>(true, logs, null, requestId);
    }

    private enum Permission {
        READ, CREATE, UPDATE, DELETE, AUDIT
    }

    private boolean hasPermission(String roleStr, Permission permission) {
        try {
            Role role = Role.valueOf(roleStr);
            switch (permission) {
                case READ: return role.canRead();
                case CREATE: return role.canCreate();
                case UPDATE: return role.canUpdate();
                case DELETE: return role.canDelete();
                case AUDIT: return role.canViewAudit();
                default: return false;
            }
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private List<Portfolio> findAllPortfolios() {
        List<Portfolio> portfolios = new ArrayList<>();
        String sql = "SELECT * FROM PORTFOLIO ORDER BY created_at DESC";
        
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                portfolios.add(mapResultSetToPortfolio(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error fetching portfolios: " + e.getMessage());
        }
        
        return portfolios;
    }

    private Portfolio findPortfolioById(String portfolioId) {
        String sql = "SELECT * FROM PORTFOLIO WHERE portfolio_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, portfolioId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToPortfolio(rs);
            }

        } catch (SQLException e) {
            System.err.println("Error fetching portfolio: " + e.getMessage());
        }
        
        return null;
    }

    private boolean insertPortfolio(Portfolio portfolio) {
        String sql = "INSERT INTO PORTFOLIO (portfolio_id, user_id, name, created_at, updated_at) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, portfolio.getPortfolioId());
            pstmt.setString(2, portfolio.getUserId());
            pstmt.setString(3, portfolio.getName());
            pstmt.setTimestamp(4, Timestamp.from(portfolio.getCreatedAt()));
            pstmt.setTimestamp(5, Timestamp.from(portfolio.getUpdatedAt()));
            
            pstmt.executeUpdate();
            conn.commit();
            return true;
            
        } catch (SQLException e) {
            System.err.println("Error inserting portfolio: " + e.getMessage());
            return false;
        }
    }

    private boolean updatePortfolio(Portfolio portfolio) {
        String sql = "UPDATE PORTFOLIO SET name = ?, updated_at = ? WHERE portfolio_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, portfolio.getName());
            pstmt.setTimestamp(2, Timestamp.from(portfolio.getUpdatedAt()));
            pstmt.setString(3, portfolio.getPortfolioId());
            
            pstmt.executeUpdate();
            conn.commit();
            return true;
            
        } catch (SQLException e) {
            System.err.println("Error updating portfolio: " + e.getMessage());
            return false;
        }
    }

    private boolean deletePortfolio(String portfolioId) {
        String sql = "DELETE FROM PORTFOLIO WHERE portfolio_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, portfolioId);
            
            pstmt.executeUpdate();
            conn.commit();
            return true;
            
        } catch (SQLException e) {
            System.err.println("Error deleting portfolio: " + e.getMessage());
            return false;
        }
    }

    private Portfolio mapResultSetToPortfolio(ResultSet rs) throws SQLException {
        Portfolio portfolio = new Portfolio();
        portfolio.setPortfolioId(rs.getString("portfolio_id"));
        portfolio.setUserId(rs.getString("user_id"));
        portfolio.setName(rs.getString("name"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            portfolio.setCreatedAt(createdAt.toInstant());
        }
        
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            portfolio.setUpdatedAt(updatedAt.toInstant());
        }
        
        return portfolio;
    }

    private List<Holding> findHoldingsByPortfolio(String portfolioId) {
        List<Holding> holdings = new ArrayList<>();
        String sql = "SELECT * FROM HOLDING WHERE portfolio_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, portfolioId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                holdings.add(mapResultSetToHolding(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error fetching holdings: " + e.getMessage());
        }
        
        return holdings;
    }

    private Holding mapResultSetToHolding(ResultSet rs) throws SQLException {
        Holding holding = new Holding();
        holding.setHoldingId(rs.getString("holding_id"));
        holding.setPortfolioId(rs.getString("portfolio_id"));
        holding.setAssetSymbol(rs.getString("asset_symbol"));
        holding.setQuantity(rs.getDouble("quantity"));
        holding.setAvgPrice(rs.getDouble("avg_price"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            holding.setCreatedAt(createdAt.toInstant());
        }
        
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            holding.setUpdatedAt(updatedAt.toInstant());
        }
        
        return holding;
    }

    private boolean insertHolding(Holding holding) {
        String sql = "INSERT INTO HOLDING (holding_id, portfolio_id, asset_symbol, quantity, avg_price, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, holding.getHoldingId());
            pstmt.setString(2, holding.getPortfolioId());
            pstmt.setString(3, holding.getAssetSymbol());
            pstmt.setDouble(4, holding.getQuantity());
            pstmt.setDouble(5, holding.getAvgPrice());
            pstmt.setTimestamp(6, Timestamp.from(holding.getCreatedAt()));
            pstmt.setTimestamp(7, Timestamp.from(holding.getUpdatedAt()));
            
            pstmt.executeUpdate();
            conn.commit();
            return true;
            
        } catch (SQLException e) {
            System.err.println("Error inserting holding: " + e.getMessage());
            return false;
        }
    }

    private List<Payment> findPaymentsByPortfolio(String portfolioId) {
        List<Payment> payments = new ArrayList<>();
        String sql = "SELECT * FROM PAYMENT WHERE portfolio_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, portfolioId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                payments.add(mapResultSetToPayment(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error fetching payments: " + e.getMessage());
        }
        
        return payments;
    }

    private Payment mapResultSetToPayment(ResultSet rs) throws SQLException {
        Payment payment = new Payment();
        payment.setPaymentId(rs.getString("payment_id"));
        payment.setPortfolioId(rs.getString("portfolio_id"));
        payment.setAmount(rs.getDouble("amount"));
        payment.setPaymentType(Payment.PaymentType.valueOf(rs.getString("payment_type")));
        payment.setStatus(Payment.PaymentStatus.valueOf(rs.getString("status")));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            payment.setCreatedAt(createdAt.toInstant());
        }
        
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            payment.setUpdatedAt(updatedAt.toInstant());
        }
        
        return payment;
    }

    private boolean insertPayment(Payment payment) {
        String sql = "INSERT INTO PAYMENT (payment_id, portfolio_id, amount, payment_type, status, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, payment.getPaymentId());
            pstmt.setString(2, payment.getPortfolioId());
            pstmt.setDouble(3, payment.getAmount());
            pstmt.setString(4, payment.getPaymentType().name());
            pstmt.setString(5, payment.getStatus().name());
            pstmt.setTimestamp(6, Timestamp.from(payment.getCreatedAt()));
            pstmt.setTimestamp(7, Timestamp.from(payment.getUpdatedAt()));
            
            pstmt.executeUpdate();
            conn.commit();
            return true;
            
        } catch (SQLException e) {
            System.err.println("Error inserting payment: " + e.getMessage());
            return false;
        }
    }
}
