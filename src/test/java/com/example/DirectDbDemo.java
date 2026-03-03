package com.example;

/**
 * Direct test of EMPController without RMI
 */
public class DirectDbDemo {
    public static void main(String[] args) {
        try {
            System.out.println("Direct database test (no RMI)...\n");

            EMPDAO dao = new EMPDAO();

            // Test 1: Count employees
            int count = dao.getAllEmployees().size();
            System.out.println("Initial employee count: " + count);

            // Test 2: Try to add an employee
            String testId = "DIRECT_TEST";
            System.out.println("\nAttempting to add employee: " + testId);
            int result = dao.addNewEmployee(testId, "Direct Test Name", "Direct Test Title");
            System.out.println("Add result: " + result);

            // Test 3: Try to find the employee
            Thread.sleep(100); // Wait a bit
            System.out.println("\nAttempting to find employee: " + testId);
            EMP found = dao.findEmployeeById(testId);
            if (found != null) {
                System.out.println("✓ Found employee: " + found.getName());
            } else {
                System.out.println("✗ Employee not found!");
            }

            // Test 4: Count again
            int newCount = dao.getAllEmployees().size();
            System.out.println("\nFinal employee count: " + newCount);
            System.out.println("Expected: " + (count + 1));

            // Cleanup
            if (found != null) {
                System.out.println("\nCleaning up...");
                dao.deleteEmployee(testId);
            }

            System.out.println("\n✓ Test completed");

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
