package com.example;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RmiSerializationTest {

    private static Registry registry;
    private static final String SERVICE_NAME = "EMPService";
    private static final int TEST_PORT = 1099;

    @BeforeAll
    static void setUp() throws Exception {
        // Start RMI registry
        registry = LocateRegistry.createRegistry(TEST_PORT);

        // Create and bind remote service
        EMPServiceImpl service = new EMPServiceImpl();
        registry.rebind(SERVICE_NAME, service);

        System.out.println("✓ RMI Registry started on port " + TEST_PORT);
        System.out.println("✓ EMPService bound to registry");
    }

    @AfterAll
    static void tearDown() throws Exception {
        // Cleanup: unbind service
        if (registry != null) {
            try {
                registry.unbind(SERVICE_NAME);
                System.out.println("✓ Service unbound from registry");
            } catch (Exception e) {
                // Ignore cleanup errors
            }
        }
    }

    @Test
    void testGetAllEmployees_Serialization() throws Exception {
        System.out.println("\n=== Testing getAllEmployees() ===");

        // Client side: lookup remote service
        EMPService remoteService = (EMPService) registry.lookup(SERVICE_NAME);
        assertNotNull(remoteService, "Remote service should not be null");

        // Invoke remote method
        List<EMP> employees = remoteService.getAllEmployees();

        // Verify results
        assertNotNull(employees, "Employee list should not be null");
        System.out.println("✓ Successfully retrieved " + employees.size() + " employees");

        // Print first 3 records (if available)
        int displayCount = Math.min(3, employees.size());
        for (int i = 0; i < displayCount; i++) {
            EMP emp = employees.get(i);
            System.out.println("  Employee " + (i + 1) + ": " +
                    emp.getENO() + " | " + emp.getName() + " | " + emp.getTitle());
        }

        System.out.println("✓ List<EMP> serialization successful");
    }

    @Test
    void testFindEmployeeById_Serialization() throws Exception {
        System.out.println("\n=== Testing findEmployeeById() ===");

        // Client side: lookup remote service
        EMPService remoteService = (EMPService) registry.lookup(SERVICE_NAME);

        // First get all employees, find a test ID
        List<EMP> allEmployees = remoteService.getAllEmployees();

        if (allEmployees != null && !allEmployees.isEmpty()) {
            String testEno = allEmployees.get(0).getENO();
            System.out.println("Testing with employee ID: " + testEno);

            // Call findEmployeeById
            EMP employee = remoteService.findEmployeeById(testEno);

            // Verify results
            assertNotNull(employee, "Employee should not be null");
            assertEquals(testEno, employee.getENO(), "Employee ID should match");

            System.out.println("✓ Found employee:");
            System.out.println("  ENO: " + employee.getENO());
            System.out.println("  ENAME: " + employee.getName());
            System.out.println("  TITLE: " + employee.getTitle());
            System.out.println("✓ Single EMP object serialization successful");
        } else {
            System.out.println("⚠ No employees in database to test with");
        }
    }

    @Test
    void testFullRmiWorkflow() throws Exception {
        System.out.println("\n=== Testing Full RMI Workflow ===");

        EMPService remoteService = (EMPService) registry.lookup(SERVICE_NAME);

        // 1. Get all employees
        List<EMP> originalList = remoteService.getAllEmployees();
        int originalCount = originalList.size();
        System.out.println("✓ Step 1: Retrieved " + originalCount + " employees");

        // 2. Add a test employee
        String testEno = "TEST001";
        int addResult = remoteService.addNewEmployee(testEno, "Test Employee", "Tester");
        assertEquals(1, addResult, "Should insert 1 row");
        System.out.println("✓ Step 2: Added test employee");

        // 3. Query the newly added employee
        EMP newEmployee = remoteService.findEmployeeById(testEno);
        assertNotNull(newEmployee, "Newly added employee should be found");
        assertEquals("Test Employee", newEmployee.getName());
        System.out.println("✓ Step 3: Found newly added employee");

        // 4. Update employee data
        int updateResult = remoteService.updateEmployee(testEno, "Updated Employee", "Senior Tester");
        assertEquals(1, updateResult, "Should update 1 row");
        System.out.println("✓ Step 4: Updated employee");

        // 5. Delete test employee
        int deleteResult = remoteService.deleteEmployee(testEno);
        assertEquals(1, deleteResult, "Should delete 1 row");
        System.out.println("✓ Step 5: Deleted test employee");

        // 6. Verify employee count is restored
        List<EMP> finalList = remoteService.getAllEmployees();
        assertEquals(originalCount, finalList.size(), "Employee count should be restored");
        System.out.println("✓ Step 6: Employee count restored to " + originalCount);

        System.out.println("✓ Full CRUD workflow via RMI successful");
    }
}