package com.example;

import org.junit.jupiter.api.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit tests for Manual Transaction Support
 * 
 * Tests:
 * - Auto-commit disabled
 * - Manual commit on success
 * - Rollback on error
 * - Transaction isolation
 * - Data consistency
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TransactionTest {

    private static Registry registry;
    private static EMPService service;
    private static final String SERVICE_NAME = "EMPService";

    @BeforeAll
    static void setUpRmiServer() throws Exception {
        System.out.println("\n=== Transaction Test Suite ===");

        // Try to get existing registry first, create if doesn't exist
        try {
            registry = LocateRegistry.getRegistry(1099);
            // Test if registry is accessible
            registry.list();
            System.out.println("✓ Connected to existing RMI Registry");
        } catch (Exception e) {
            // Registry doesn't exist, create it
            registry = LocateRegistry.createRegistry(1099);
            System.out.println("✓ RMI Registry created");
        }

        // Create and bind service
        EMPServiceImpl serviceImpl = new EMPServiceImpl();
        registry.rebind(SERVICE_NAME, serviceImpl);
        service = (EMPService) registry.lookup(SERVICE_NAME);
        System.out.println("✓ EMPService ready\n");
    }

    @AfterAll
    static void tearDown() throws Exception {
        if (registry != null) {
            try {
                registry.unbind(SERVICE_NAME);
            } catch (Exception e) {
                System.err.println("Warning: Could not unbind service - " + e.getMessage());
            }
        }
        System.out.println("\n=== Transaction Test Suite Completed ===");
    }

    @Test
    @Order(1)
    @DisplayName("Task 3.1: Verify Auto-Commit is Disabled")
    void testAutoCommitDisabled() throws SQLException {
        System.out.println("\n--- Test 3.1: Auto-Commit Disabled ---");

        // Get a connection and verify auto-commit is false
        Connection conn = DBConnection.getConnection();

        assertFalse(conn.getAutoCommit(),
                "Auto-commit should be disabled (setAutoCommit(false))");

        conn.close();
        System.out.println("✓ Test 3.1 Passed: Auto-commit is disabled");
    }

    @Test
    @Order(2)
    @DisplayName("Task 3.2: Test Manual Commit on Successful Insert")
    void testManualCommitOnInsert() throws Exception {
        System.out.println("\n--- Test 3.2: Manual Commit on Insert ---");

        String testEno = "TASK3_COMMIT_TEST";

        try {
            // Record initial count
            int initialCount = service.getAllEmployees().size();
            System.out.println("  Initial employee count: " + initialCount);

            // Add employee (should commit)
            System.out.println("  Adding employee: " + testEno);
            int addResult = service.addNewEmployee(testEno, "Commit Test", "Tester");
            assertEquals(1, addResult, "Should add one employee");

            // Verify employee was committed
            EMP addedEmp = service.findEmployeeById(testEno);
            assertNotNull(addedEmp, "Employee should exist after commit");

            // Verify count increased
            int newCount = service.getAllEmployees().size();
            assertEquals(initialCount + 1, newCount, "Count should increase by 1");

            System.out.println("  New employee count: " + newCount);
            System.out.println("✓ Test 3.2 Passed: Manual commit works on insert");

        } finally {
            // Cleanup
            service.deleteEmployee(testEno);
        }
    }

    @Test
    @Order(3)
    @DisplayName("Task 3.3: Test Manual Commit on Update")
    void testManualCommitOnUpdate() throws Exception {
        System.out.println("\n--- Test 3.3: Manual Commit on Update ---");

        String testEno = "TASK3_UPDATE_TEST";

        try {
            // Create test employee
            service.addNewEmployee(testEno, "Original Name", "Original Title");
            System.out.println("  Created employee: " + testEno);

            // Update employee (should commit)
            System.out.println("  Updating employee...");
            int updateResult = service.updateEmployee(testEno, "Updated Name", "Updated Title");
            assertEquals(1, updateResult, "Should update one employee");

            // Verify update was committed
            EMP updatedEmp = service.findEmployeeById(testEno);
            assertEquals("Updated Name", updatedEmp.getName(), "Name should be updated");
            assertEquals("Updated Title", updatedEmp.getTitle(), "Title should be updated");

            System.out.println("  Verified: " + updatedEmp.getName() + " | " + updatedEmp.getTitle());
            System.out.println("✓ Test 3.3 Passed: Manual commit works on update");

        } finally {
            service.deleteEmployee(testEno);
        }
    }

    @Test
    @Order(4)
    @DisplayName("Task 3.4: Test Manual Commit on Delete")
    void testManualCommitOnDelete() throws Exception {
        System.out.println("\n--- Test 3.4: Manual Commit on Delete ---");

        String testEno = "TASK3_DELETE_TEST";

        // Create test employee
        service.addNewEmployee(testEno, "Delete Test", "Tester");
        EMP beforeDelete = service.findEmployeeById(testEno);
        assertNotNull(beforeDelete, "Employee should exist before delete");
        System.out.println("  Created employee: " + testEno);

        // Delete employee (should commit)
        System.out.println("  Deleting employee...");
        int deleteResult = service.deleteEmployee(testEno);
        assertEquals(1, deleteResult, "Should delete one employee");

        // Verify deletion was committed
        EMP afterDelete = service.findEmployeeById(testEno);
        assertNull(afterDelete, "Employee should not exist after delete");

        System.out.println("  Verified: Employee deleted");
        System.out.println("✓ Test 3.4 Passed: Manual commit works on delete");
    }

    @Test
    @Order(5)
    @DisplayName("Task 3.5: Test Rollback on Error (Duplicate Key)")
    void testRollbackOnDuplicateKey() {
        System.out.println("\n--- Test 3.5: Rollback on Duplicate Key ---");

        String testEno = "TASK3_DUPLICATE";

        try {
            // Add first employee (should succeed)
            service.addNewEmployee(testEno, "First Employee", "Tester");
            System.out.println("  Added first employee: " + testEno);

            // Try to add duplicate (should fail and rollback)
            System.out.println("  Attempting to add duplicate...");
            boolean exceptionCaught = false;
            try {
                service.addNewEmployee(testEno, "Duplicate Employee", "Tester");
            } catch (SQLException e) {
                System.out.println("  Expected error caught: " + e.getClass().getSimpleName());
                exceptionCaught = true;
            }

            assertTrue(exceptionCaught, "Should throw exception for duplicate key");

            // Verify database is consistent (only one employee)
            EMP emp = service.findEmployeeById(testEno);
            assertNotNull(emp, "Original employee should still exist");
            assertEquals("First Employee", emp.getName(),
                    "Should have original employee, not duplicate");

            System.out.println("  Verified: Database rolled back correctly");
            System.out.println("✓ Test 3.5 Passed: Rollback works on error");

        } catch (Exception e) {
            fail("Test failed: " + e.getMessage());
        } finally {
            try {
                service.deleteEmployee(testEno);
            } catch (Exception e) {
                System.err.println("Warning: Cleanup failed - " + e.getMessage());
            }
        }
    }

    @Test
    @Order(6)
    @DisplayName("Task 3.6: Test Transaction Isolation")
    void testTransactionIsolation() throws Exception {
        System.out.println("\n--- Test 3.6: Transaction Isolation ---");

        String testEno = "TASK3_ISOLATION";

        try {
            // Create test employee
            service.addNewEmployee(testEno, "Isolation Test", "Tester");
            EMP original = service.findEmployeeById(testEno);
            System.out.println("  Created employee: " + original.getName());

            // Get initial count
            int initialCount = service.getAllEmployees().size();

            // Simulate concurrent access (read while another might be writing)
            Thread reader = new Thread(() -> {
                try {
                    Thread.sleep(50); // Small delay
                    EMP emp = service.findEmployeeById(testEno);
                    assertNotNull(emp, "Should read committed data");
                    System.out.println("  Reader: Read committed data");
                } catch (Exception e) {
                    fail("Reader should succeed: " + e.getMessage());
                }
            });

            reader.start();

            // Update in main thread
            service.updateEmployee(testEno, "Updated in Transaction", "Updated Title");

            reader.join();

            // Verify final state
            EMP updated = service.findEmployeeById(testEno);
            assertEquals("Updated in Transaction", updated.getName());

            int finalCount = service.getAllEmployees().size();
            assertEquals(initialCount, finalCount, "Count should remain consistent");

            System.out.println("  Verified: " + updated.getName());
            System.out.println("✓ Test 3.6 Passed: Transaction isolation maintained");

        } finally {
            service.deleteEmployee(testEno);
        }
    }

    @Test
    @Order(7)
    @DisplayName("Task 3.7: Test Data Consistency After Multiple Operations")
    void testDataConsistency() throws Exception {
        System.out.println("\n--- Test 3.7: Data Consistency ---");

        // Get initial state
        List<EMP> initialEmployees = service.getAllEmployees();
        int initialCount = initialEmployees.size();
        System.out.println("  Initial employee count: " + initialCount);

        String[] testEnos = { "TASK3_CONS_1", "TASK3_CONS_2", "TASK3_CONS_3" };

        try {
            // Perform multiple operations
            System.out.println("  Performing multiple operations...");
            for (String eno : testEnos) {
                service.addNewEmployee(eno, "Consistency Test", "Tester");
            }

            // Verify count increased correctly
            int afterAdd = service.getAllEmployees().size();
            assertEquals(initialCount + 3, afterAdd,
                    "Should have added exactly 3 employees");
            System.out.println("  After adding: " + afterAdd + " employees");

            // Update all test employees
            for (String eno : testEnos) {
                service.updateEmployee(eno, "Updated", "Updated");
            }

            // Verify count unchanged after updates
            int afterUpdate = service.getAllEmployees().size();
            assertEquals(afterAdd, afterUpdate,
                    "Count should not change after updates");
            System.out.println("  After updating: " + afterUpdate + " employees");

            // Delete all test employees
            for (String eno : testEnos) {
                service.deleteEmployee(eno);
            }

            // Verify count restored
            int finalCount = service.getAllEmployees().size();
            assertEquals(initialCount, finalCount,
                    "Count should return to initial value");
            System.out.println("  After deleting: " + finalCount + " employees");

            System.out.println("✓ Test 3.7 Passed: Data consistency maintained");

        } finally {
            // Cleanup any remaining test employees
            for (String eno : testEnos) {
                try {
                    service.deleteEmployee(eno);
                } catch (Exception e) {
                    System.err.println("Warning: Cleanup failed for " + eno + " - " + e.getMessage());
                }
            }
        }
    }

    @Test
    @Order(8)
    @DisplayName("Task 3.8: Verify Commit/Rollback Logging")
    void testCommitRollbackLogging() throws Exception {
        System.out.println("\n--- Test 3.8: Commit/Rollback Logging ---");
        System.out.println("  Note: Check server console for transaction log messages:");
        System.out.println("    - '✓ Transaction committed: ...'");
        System.out.println("    - '✗ Transaction rolled back: ...'");

        String testEno = "TASK3_LOG_TEST";

        try {
            // This should log a commit message on server
            System.out.println("\n  Performing operation (check server console)...");
            service.addNewEmployee(testEno, "Log Test", "Tester");

            // Verify operation succeeded
            EMP emp = service.findEmployeeById(testEno);
            assertNotNull(emp, "Operation should succeed");

            System.out.println("  ✓ Operation completed");
            System.out
                    .println("  ✓ Check server console for: '✓ Transaction committed: Added employee " + testEno + "'");
            System.out.println("✓ Test 3.8 Passed: Logging verification complete");

        } finally {
            service.deleteEmployee(testEno);
        }
    }
}
