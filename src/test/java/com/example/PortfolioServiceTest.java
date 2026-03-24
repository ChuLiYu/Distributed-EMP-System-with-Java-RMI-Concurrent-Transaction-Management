package com.example;

import org.junit.jupiter.api.*;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PortfolioServiceTest {

    private static PortfolioService service;
    private static String testRequestId;

    @BeforeAll
    static void setUp() {
        System.out.println("\n=== Portfolio Service Test Suite ===");
        
        DatabaseInitializer.initialize();
        
        service = new PortfolioService();
        testRequestId = UUID.randomUUID().toString();
    }

    @Test
    @Order(1)
    @DisplayName("Test 1: Admin Login")
    void testAdminLogin() {
        System.out.println("\n--- Test 1: Admin Login ---");
        
        PortfolioService.ServiceResult<String> result = service.login("admin", "admin123", testRequestId);
        
        assertTrue(result.isSuccess(), "Admin login should succeed");
        assertEquals("admin-001", result.getData());
        System.out.println("  Admin logged in as: " + result.getData());
        System.out.println("✓ Test 1 Passed");
    }

    @Test
    @Order(2)
    @DisplayName("Test 2: Invalid Login")
    void testInvalidLogin() {
        System.out.println("\n--- Test 2: Invalid Login ---");
        
        String reqId = UUID.randomUUID().toString();
        PortfolioService.ServiceResult<String> result = service.login("admin", "wrongpassword", reqId);
        
        assertFalse(result.isSuccess(), "Invalid login should fail");
        assertEquals("INVALID_PASSWORD", result.getErrorCode());
        System.out.println("  Login failed with error: " + result.getErrorCode());
        System.out.println("✓ Test 2 Passed");
    }

    @Test
    @Order(3)
    @DisplayName("Test 3: Create Portfolio as Admin")
    void testCreatePortfolioAsAdmin() {
        System.out.println("\n--- Test 3: Create Portfolio as Admin ---");
        
        String reqId = UUID.randomUUID().toString();
        String portfolioId = "TEST_PF_" + System.currentTimeMillis();
        
        PortfolioService.ServiceResult<Portfolio> result = service.createPortfolio(
            "admin-001", "ADMIN", reqId, portfolioId, "Test Portfolio"
        );
        
        assertTrue(result.isSuccess(), "Portfolio creation should succeed");
        assertNotNull(result.getData());
        assertEquals(portfolioId, result.getData().getPortfolioId());
        System.out.println("  Created portfolio: " + result.getData().getPortfolioId());
        System.out.println("✓ Test 3 Passed");
    }

    @Test
    @Order(4)
    @DisplayName("Test 4: Viewer Cannot Create Portfolio")
    void testViewerCannotCreatePortfolio() {
        System.out.println("\n--- Test 4: Viewer Cannot Create Portfolio ---");
        
        String reqId = UUID.randomUUID().toString();
        String portfolioId = "VIEWER_PF_" + System.currentTimeMillis();
        
        PortfolioService.ServiceResult<Portfolio> result = service.createPortfolio(
            "viewer-001", "VIEWER", reqId, portfolioId, "Viewer Portfolio"
        );
        
        assertFalse(result.isSuccess(), "Viewer should not be able to create portfolio");
        assertEquals("ACCESS_DENIED", result.getErrorCode());
        System.out.println("  Access denied as expected: " + result.getErrorCode());
        System.out.println("✓ Test 4 Passed");
    }

    @Test
    @Order(5)
    @DisplayName("Test 5: Operator Can Create Portfolio")
    void testOperatorCanCreatePortfolio() {
        System.out.println("\n--- Test 5: Operator Can Create Portfolio ---");
        
        String reqId = UUID.randomUUID().toString();
        String portfolioId = "OP_PF_" + System.currentTimeMillis();
        
        PortfolioService.ServiceResult<Portfolio> result = service.createPortfolio(
            "op-001", "OPERATOR", reqId, portfolioId, "Operator Portfolio"
        );
        
        assertTrue(result.isSuccess(), "Operator should be able to create portfolio");
        System.out.println("  Created portfolio: " + result.getData().getPortfolioId());
        System.out.println("✓ Test 5 Passed");
    }

    @Test
    @Order(6)
    @DisplayName("Test 6: Viewer Can Read Portfolio")
    void testViewerCanReadPortfolio() {
        System.out.println("\n--- Test 6: Viewer Can Read Portfolio ---");
        
        String reqId = UUID.randomUUID().toString();
        
        PortfolioService.ServiceResult<List<Portfolio>> result = service.getPortfolios(
            "viewer-001", "VIEWER", reqId
        );
        
        assertTrue(result.isSuccess(), "Viewer should be able to read portfolios");
        assertNotNull(result.getData());
        System.out.println("  Retrieved " + result.getData().size() + " portfolios");
        System.out.println("✓ Test 6 Passed");
    }

    @Test
    @Order(7)
    @DisplayName("Test 7: Input Validation - Empty Portfolio ID")
    void testValidationEmptyPortfolioId() {
        System.out.println("\n--- Test 7: Input Validation ---");
        
        String reqId = UUID.randomUUID().toString();
        
        PortfolioService.ServiceResult<Portfolio> result = service.createPortfolio(
            "admin-001", "ADMIN", reqId, "", "Test Portfolio"
        );
        
        assertFalse(result.isSuccess(), "Empty portfolio ID should fail validation");
        assertTrue(result.getErrorCode().contains("INVALID_INPUT"));
        System.out.println("  Validation failed: " + result.getErrorCode());
        System.out.println("✓ Test 7 Passed");
    }

    @Test
    @Order(8)
    @DisplayName("Test 8: Input Validation - Invalid Asset Symbol")
    void testValidationInvalidAssetSymbol() {
        System.out.println("\n--- Test 8: Input Validation - Asset Symbol ---");
        
        String reqId = UUID.randomUUID().toString();
        String portfolioId = "PF_VAL_" + System.currentTimeMillis();
        
        service.createPortfolio("admin-001", "ADMIN", reqId, portfolioId, "Validation Test");
        
        String holdingReqId = UUID.randomUUID().toString();
        PortfolioService.ServiceResult<Holding> holdingResult = service.addHolding(
            "admin-001", "ADMIN", holdingReqId, 
            "HLD_INVALID", portfolioId, "invalid-symbol!", 100, 50.0
        );
        
        assertFalse(holdingResult.isSuccess(), "Invalid asset symbol should fail");
        assertTrue(holdingResult.getErrorCode().contains("INVALID_INPUT"));
        System.out.println("  Validation failed: " + holdingResult.getErrorCode());
        System.out.println("✓ Test 8 Passed");
    }

    @Test
    @Order(9)
    @DisplayName("Test 9: Add Holding")
    void testAddHolding() {
        System.out.println("\n--- Test 9: Add Holding ---");
        
        String pfReqId = UUID.randomUUID().toString();
        String portfolioId = "PF_HLD_" + System.currentTimeMillis();
        
        service.createPortfolio("admin-001", "ADMIN", pfReqId, portfolioId, "Holding Test");
        
        String holdingReqId = UUID.randomUUID().toString();
        PortfolioService.ServiceResult<Holding> result = service.addHolding(
            "admin-001", "ADMIN", holdingReqId,
            "HLD_" + System.currentTimeMillis(), portfolioId, "AAPL", 100, 150.0
        );
        
        assertTrue(result.isSuccess(), "Holding creation should succeed");
        assertNotNull(result.getData());
        assertEquals("AAPL", result.getData().getAssetSymbol());
        System.out.println("  Added holding: " + result.getData().getAssetSymbol());
        System.out.println("✓ Test 9 Passed");
    }

    @Test
    @Order(10)
    @DisplayName("Test 10: Create Payment")
    void testCreatePayment() {
        System.out.println("\n--- Test 10: Create Payment ---");
        
        String pfReqId = UUID.randomUUID().toString();
        String portfolioId = "PF_PAY_" + System.currentTimeMillis();
        
        service.createPortfolio("admin-001", "ADMIN", pfReqId, portfolioId, "Payment Test");
        
        String payReqId = UUID.randomUUID().toString();
        PortfolioService.ServiceResult<Payment> result = service.createPayment(
            "admin-001", "ADMIN", payReqId,
            "PAY_" + System.currentTimeMillis(), portfolioId, 1000.0, Payment.PaymentType.DEPOSIT
        );
        
        assertTrue(result.isSuccess(), "Payment creation should succeed");
        assertNotNull(result.getData());
        assertEquals(Payment.PaymentType.DEPOSIT, result.getData().getPaymentType());
        System.out.println("  Created payment: " + result.getData().getPaymentId());
        System.out.println("✓ Test 10 Passed");
    }

    @Test
    @Order(11)
    @DisplayName("Test 11: Delete Portfolio as Supervisor")
    void testDeletePortfolioAsSupervisor() {
        System.out.println("\n--- Test 11: Delete Portfolio as Supervisor ---");
        
        String pfReqId = UUID.randomUUID().toString();
        String portfolioId = "PF_DEL_" + System.currentTimeMillis();
        
        service.createPortfolio("admin-001", "ADMIN", pfReqId, portfolioId, "Delete Test");
        
        String delReqId = UUID.randomUUID().toString();
        PortfolioService.ServiceResult<Boolean> result = service.deletePortfolio(
            "sup-001", "SUPERVISOR", delReqId, portfolioId
        );
        
        assertTrue(result.isSuccess(), "Supervisor should be able to delete portfolio");
        System.out.println("  Deleted portfolio: " + portfolioId);
        System.out.println("✓ Test 11 Passed");
    }

    @Test
    @Order(12)
    @DisplayName("Test 12: Operator Cannot Delete Portfolio")
    void testOperatorCannotDeletePortfolio() {
        System.out.println("\n--- Test 12: Operator Cannot Delete Portfolio ---");
        
        String pfReqId = UUID.randomUUID().toString();
        String portfolioId = "PF_NODEL_" + System.currentTimeMillis();
        
        service.createPortfolio("admin-001", "ADMIN", pfReqId, portfolioId, "No Delete Test");
        
        String delReqId = UUID.randomUUID().toString();
        PortfolioService.ServiceResult<Boolean> result = service.deletePortfolio(
            "op-001", "OPERATOR", delReqId, portfolioId
        );
        
        assertFalse(result.isSuccess(), "Operator should not be able to delete");
        assertEquals("ACCESS_DENIED", result.getErrorCode());
        System.out.println("  Access denied as expected: " + result.getErrorCode());
        System.out.println("✓ Test 12 Passed");
    }

    @Test
    @Order(13)
    @DisplayName("Test 13: Audit Logs Accessible by Admin")
    void testAuditLogsAccessibleByAdmin() {
        System.out.println("\n--- Test 13: Audit Logs ---");
        
        String reqId = UUID.randomUUID().toString();
        
        PortfolioService.ServiceResult<List<AuditEvent>> result = service.getAuditLogs(
            "admin-001", "ADMIN", reqId, "PORTFOLIO", null
        );
        
        assertTrue(result.isSuccess(), "Admin should access audit logs");
        assertNotNull(result.getData());
        System.out.println("  Retrieved " + result.getData().size() + " audit events");
        System.out.println("✓ Test 13 Passed");
    }

    @Test
    @Order(14)
    @DisplayName("Test 14: Audit Logs Not Accessible by Viewer")
    void testAuditLogsNotAccessibleByViewer() {
        System.out.println("\n--- Test 14: Audit Logs Access Denied ---");
        
        String reqId = UUID.randomUUID().toString();
        
        PortfolioService.ServiceResult<List<AuditEvent>> result = service.getAuditLogs(
            "viewer-001", "VIEWER", reqId, "PORTFOLIO", null
        );
        
        assertFalse(result.isSuccess(), "Viewer should not access audit logs");
        assertEquals("ACCESS_DENIED", result.getErrorCode());
        System.out.println("  Access denied as expected: " + result.getErrorCode());
        System.out.println("✓ Test 14 Passed");
    }

    @Test
    @Order(15)
    @DisplayName("Test 15: Supervisor Can View Audit (Read-only)")
    void testSupervisorCanViewAudit() {
        System.out.println("\n--- Test 15: Supervisor Audit Access ---");
        
        String reqId = UUID.randomUUID().toString();
        
        PortfolioService.ServiceResult<List<AuditEvent>> result = service.getAuditLogs(
            "sup-001", "SUPERVISOR", reqId, null, null
        );
        
        assertTrue(result.isSuccess(), "Supervisor should view audit logs");
        System.out.println("  Supervisor can view audit logs");
        System.out.println("✓ Test 15 Passed");
    }
}
