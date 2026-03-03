package com.example;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Simple test to debug database operations
 */
public class SimpleDbDemo {
    public static void main(String[] args) {
        try {
            System.out.println("Starting simple database test...\n");

            // Setup RMI
            Registry registry;
            try {
                registry = LocateRegistry.getRegistry(1099);
                registry.list();
                System.out.println("✓ Connected to existing RMI Registry");
            } catch (Exception e) {
                registry = LocateRegistry.createRegistry(1099);
                System.out.println("✓ RMI Registry created");
            }

            // Create and bind service
            EMPServiceImpl serviceImpl = new EMPServiceImpl();
            registry.rebind("EMPService", serviceImpl);
            EMPService service = (EMPService) registry.lookup("EMPService");
            System.out.println("✓ EMPService ready\n");

            // Test 1: Count employees
            int count = service.getAllEmployees().size();
            System.out.println("Initial employee count: " + count);

            // Test 2: Try to add an employee
            String testId = "SIMPLE_TEST";
            System.out.println("\nAttempting to add employee: " + testId);
            int result = service.addNewEmployee(testId, "Test Name", "Test Title");
            System.out.println("Add result: " + result);

            // Wait a bit to ensure commit completes
            Thread.sleep(100);

            // Test 3: Try to find the employee
            System.out.println("\nAttempting to find employee: " + testId);
            EMP found = service.findEmployeeById(testId);
            if (found != null) {
                System.out.println("✓ Found employee: " + found.getName());
            } else {
                System.out.println("✗ Employee not found!");

                // Let's also try to get all employees and see if it's there
                System.out.println("\nLet me check all employees:");
                for (EMP emp : service.getAllEmployees()) {
                    if (emp.getENO().startsWith("SIMPLE")) {
                        System.out.println("  Found: " + emp.getENO() + " - " + emp.getName());
                    }
                }
            }

            // Test 4: Count again
            int newCount = service.getAllEmployees().size();
            System.out.println("\nFinal employee count: " + newCount);
            System.out.println("Expected: " + (count + 1));

            // Cleanup
            System.out.println("\nCleaning up...");
            service.deleteEmployee(testId);

            registry.unbind("EMPService");
            System.out.println("\n✓ Test completed");

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
