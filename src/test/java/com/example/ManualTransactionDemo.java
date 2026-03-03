package com.example;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;

/**
 * Test client to verify manual transaction support (Task 3)
 */
public class ManualTransactionDemo {

    public static void main(String[] args) {
        try {
            // Connect to RMI Registry
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            EMPService service = (EMPService) registry.lookup("EMPService");

            System.out.println("=== Testing Manual Transaction Support (Task 3) ===\n");

            // Test 1: Successful transaction
            testSuccessfulTransaction(service);

            // Test 2: Show all employees
            testListAllEmployees(service);

            // Test 3: Update and rollback scenario
            testUpdateTransaction(service);

            System.out.println("\n=== Transaction Tests Completed ===");

        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
    }

    private static void testSuccessfulTransaction(EMPService service) {
        System.out.println("--- Test 1: Successful Add Transaction ---");
        try {
            String testEno = "TEST001";
            int result = service.addNewEmployee(testEno, "Test Employee", "Test Title");

            if (result > 0) {
                System.out.println("✓ Employee added successfully");

                // Verify the employee was added
                EMP emp = service.findEmployeeById(testEno);
                if (emp != null) {
                    System.out.println("✓ Employee verified: " + emp.getName());
                }

                // Clean up
                service.deleteEmployee(testEno);
                System.out.println("✓ Test employee cleaned up\n");
            }
        } catch (Exception e) {
            System.err.println("✗ Test failed: " + e.getMessage());
        }
    }

    private static void testListAllEmployees(EMPService service) {
        System.out.println("--- Test 2: List All Employees ---");
        try {
            List<EMP> employees = service.getAllEmployees();
            System.out.println("✓ Retrieved " + employees.size() + " employees");

            // Display first 3
            int count = Math.min(3, employees.size());
            for (int i = 0; i < count; i++) {
                EMP emp = employees.get(i);
                System.out.println("  " + emp.getENO() + " | " +
                        emp.getName() + " | " + emp.getTitle());
            }
            System.out.println();
        } catch (Exception e) {
            System.err.println("✗ Test failed: " + e.getMessage());
        }
    }

    private static void testUpdateTransaction(EMPService service) {
        System.out.println("--- Test 3: Update Transaction ---");
        try {
            // Get first employee
            List<EMP> employees = service.getAllEmployees();
            if (!employees.isEmpty()) {
                EMP original = employees.get(0);
                String eno = original.getENO();

                System.out.println("Original: " + original.getName() + " | " + original.getTitle());

                // Update
                service.updateEmployee(eno, "Updated Name", "Updated Title");
                EMP updated = service.findEmployeeById(eno);
                System.out.println("Updated:  " + updated.getName() + " | " + updated.getTitle());

                // Restore
                service.updateEmployee(eno, original.getName(), original.getTitle());
                System.out.println("✓ Restored to original state\n");
            }
        } catch (Exception e) {
            System.err.println("✗ Test failed: " + e.getMessage());
        }
    }
}