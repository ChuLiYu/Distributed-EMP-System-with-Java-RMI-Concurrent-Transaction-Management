package com.example;

import org.junit.jupiter.api.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit tests for RMI Distributed System
 * 
 * Tests:
 * - RMI server setup
 * - Remote method invocation
 * - Object serialization (EMP, List<EMP>)
 * - Multiple client access
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RmiTest {

    private static Registry registry;
    private static EMPService service;
    private static final int RMI_PORT = 1099;
    private static final String SERVICE_NAME = "EMPService";

    @BeforeAll
    static void setUpRmiServer() throws Exception {
        System.out.println("\n=== RMI Test Suite ===");

        // Try to get existing registry first, create if doesn't exist
        try {
            registry = LocateRegistry.getRegistry(RMI_PORT);
            // Test if registry is accessible
            registry.list();
            System.out.println("✓ Connected to existing RMI Registry on port " + RMI_PORT);
        } catch (Exception e) {
            // Registry doesn't exist, create it
            registry = LocateRegistry.createRegistry(RMI_PORT);
            System.out.println("✓ RMI Registry created on port " + RMI_PORT);
        }

        // Create and bind service
        EMPServiceImpl serviceImpl = new EMPServiceImpl();
        registry.rebind(SERVICE_NAME, serviceImpl);
        System.out.println("✓ EMPService bound to registry");

        // Look up the service (simulate client)
        service = (EMPService) registry.lookup(SERVICE_NAME);
        System.out.println("✓ EMPService looked up successfully\n");
    }

    @AfterAll
    static void tearDownRmiServer() throws Exception {
        if (registry != null) {
            try {
                registry.unbind(SERVICE_NAME);
                System.out.println("\n✓ EMPService unbound from registry");
            } catch (Exception e) {
                System.err.println("Warning: Could not unbind service: " + e.getMessage());
            }
        }
        System.out.println("\n=== RMI Test Suite Completed ===");
    }

    @Test
    @Order(1)
    @DisplayName("Task 2.1: Test RMI Server Setup")
    void testRmiServerSetup() {
        assertNotNull(registry, "RMI Registry should be created");
        assertNotNull(service, "EMPService should be bound and accessible");
        System.out.println("✓ Test 2.1 Passed: RMI server setup successful");
    }

    @Test
    @Order(2)
    @DisplayName("Task 2.2: Test Single EMP Object Serialization")
    void testSingleEmpSerialization() throws Exception {
        System.out.println("\n--- Test 2.2: Single EMP Serialization ---");

        // Find an existing employee
        EMP emp = service.findEmployeeById("E1");

        // Verify serialization worked
        assertNotNull(emp, "Employee should be found");
        assertEquals("E1", emp.getENO(), "Employee ID should match");
        assertNotNull(emp.getName(), "Employee name should not be null");
        assertNotNull(emp.getTitle(), "Employee title should not be null");

        System.out.println("  Employee: " + emp.getENO() + " | " +
                emp.getName() + " | " + emp.getTitle());
        System.out.println("✓ Test 2.2 Passed: EMP object serialized successfully");
    }

    @Test
    @Order(3)
    @DisplayName("Task 2.3: Test List<EMP> Serialization")
    void testListEmpSerialization() throws Exception {
        System.out.println("\n--- Test 2.3: List<EMP> Serialization ---");

        // Get all employees
        List<EMP> employees = service.getAllEmployees();

        // Verify serialization worked
        assertNotNull(employees, "Employee list should not be null");
        assertTrue(employees.size() > 0, "Should have at least one employee");

        // Verify each employee is properly serialized
        for (EMP emp : employees) {
            assertNotNull(emp.getENO(), "Employee ID should not be null");
            assertNotNull(emp.getName(), "Employee name should not be null");
        }

        System.out.println("  Retrieved " + employees.size() + " employees");
        System.out.println("  First 3 employees:");
        for (int i = 0; i < Math.min(3, employees.size()); i++) {
            EMP emp = employees.get(i);
            System.out.println("    " + emp.getENO() + " | " +
                    emp.getName() + " | " + emp.getTitle());
        }
        System.out.println("✓ Test 2.3 Passed: List<EMP> serialized successfully");
    }

    @Test
    @Order(4)
    @DisplayName("Task 2.4: Test Remote CRUD Operations")
    void testRemoteCrudOperations() throws Exception {
        System.out.println("\n--- Test 2.4: Remote CRUD Operations ---");

        String testEno = "TASK2_TEST";

        try {
            // CREATE
            System.out.println("  Testing CREATE...");
            int addResult = service.addNewEmployee(testEno, "Task2 Test Employee", "Tester");
            assertEquals(1, addResult, "Should add one employee");

            // READ
            System.out.println("  Testing READ...");
            EMP createdEmp = service.findEmployeeById(testEno);
            assertNotNull(createdEmp, "Employee should be created");
            assertEquals("Task2 Test Employee", createdEmp.getName());

            // UPDATE
            System.out.println("  Testing UPDATE...");
            int updateResult = service.updateEmployee(testEno, "Updated Employee", "Senior Tester");
            assertEquals(1, updateResult, "Should update one employee");

            EMP updatedEmp = service.findEmployeeById(testEno);
            assertEquals("Updated Employee", updatedEmp.getName());

            // DELETE
            System.out.println("  Testing DELETE...");
            int deleteResult = service.deleteEmployee(testEno);
            assertEquals(1, deleteResult, "Should delete one employee");

            EMP deletedEmp = service.findEmployeeById(testEno);
            assertNull(deletedEmp, "Employee should be deleted");

            System.out.println("✓ Test 2.4 Passed: All CRUD operations work remotely");

        } finally {
            // Cleanup: ensure test employee is deleted
            try {
                service.deleteEmployee(testEno);
            } catch (Exception e) {
                System.err.println("Warning: Cleanup failed for " + testEno + " - " + e.getMessage());
            }
        }
    }

    @Test
    @Order(5)
    @DisplayName("Task 2.5: Test Multiple Client Access (Simulated)")
    void testMultipleClientAccess() throws Exception {
        System.out.println("\n--- Test 2.5: Multiple Client Access ---");

        // Simulate multiple clients accessing the same service
        Thread[] clients = new Thread[3];
        final boolean[] success = { true, true, true };

        for (int i = 0; i < 3; i++) {
            final int clientId = i;
            clients[i] = new Thread(() -> {
                try {
                    // Each "client" looks up the service
                    EMPService clientService = (EMPService) registry.lookup(SERVICE_NAME);

                    // Perform operations
                    List<EMP> employees = clientService.getAllEmployees();

                    System.out.println("  Client-" + (clientId + 1) +
                            ": Retrieved " + employees.size() + " employees");

                    if (employees.size() == 0) {
                        success[clientId] = false;
                    }

                } catch (Exception e) {
                    System.err.println("  Client-" + (clientId + 1) + " error: " + e.getMessage());
                    success[clientId] = false;
                }
            });
        }

        // Start all clients
        for (Thread client : clients) {
            client.start();
        }

        // Wait for all clients to complete
        for (Thread client : clients) {
            client.join();
        }

        // Verify all clients succeeded
        assertTrue(success[0] && success[1] && success[2],
                "All clients should access the service successfully");

        System.out.println("✓ Test 2.5 Passed: Multiple clients can access service concurrently");
    }

    @Test
    @Order(6)
    @DisplayName("Task 2.6: Test RMI Exception Handling")
    void testRmiExceptionHandling() throws Exception {
        System.out.println("\n--- Test 2.6: RMI Exception Handling ---");

        // Test with non-existent employee
        EMP nonExistent = service.findEmployeeById("NONEXISTENT999");
        assertNull(nonExistent, "Non-existent employee should return null");

        // Test update non-existent employee
        int updateResult = service.updateEmployee("NONEXISTENT999", "Name", "Title");
        assertEquals(0, updateResult, "Update should return 0 for non-existent employee");

        // Test delete non-existent employee
        int deleteResult = service.deleteEmployee("NONEXISTENT999");
        assertEquals(0, deleteResult, "Delete should return 0 for non-existent employee");

        System.out.println("✓ Test 2.6 Passed: Exception handling works correctly");
    }
}
