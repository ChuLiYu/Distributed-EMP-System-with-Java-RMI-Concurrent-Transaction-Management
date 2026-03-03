package com.example;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Scanner;

public class EMPClient {
    private static EMPService service;
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        try {
            // Connect to RMI Registry
            String host = (args.length < 1) ? "localhost" : args[0];
            Registry registry = LocateRegistry.getRegistry(host, 1099);
            service = (EMPService) registry.lookup("EMPService");

            System.out.println("✓ Connected to EMPService on " + host);
            System.out.println("================================");

            // Interactive menu
            showMenu();

        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
    }

    private static void showMenu() {
        while (true) {
            System.out.println("\n=== EMP Client Menu ===");
            System.out.println("1. List all employees");
            System.out.println("2. Find employee by ID");
            System.out.println("3. Add new employee");
            System.out.println("4. Update employee");
            System.out.println("5. Delete employee");
            System.out.println("6. Exit");
            System.out.print("Choose option: ");

            try {
                int choice = Integer.parseInt(scanner.nextLine());

                switch (choice) {
                    case 1:
                        listAllEmployees();
                        break;
                    case 2:
                        findEmployee();
                        break;
                    case 3:
                        addEmployee();
                        break;
                    case 4:
                        updateEmployee();
                        break;
                    case 5:
                        deleteEmployee();
                        break;
                    case 6:
                        System.out.println("Goodbye!");
                        return;
                    default:
                        System.out.println("Invalid option");
                }
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
    }

    private static void listAllEmployees() throws Exception {
        List<EMP> employees = service.getAllEmployees();
        System.out.println("\n=== All Employees ===");
        System.out.printf("%-10s %-20s %-15s%n", "ENO", "ENAME", "TITLE");
        System.out.println("------------------------------------------------");
        for (EMP emp : employees) {
            System.out.printf("%-10s %-20s %-15s%n",
                    emp.getENO(), emp.getName(), emp.getTitle());
        }
        System.out.println("Total: " + employees.size() + " employees");
    }

    private static void findEmployee() throws Exception {
        System.out.print("Enter Employee ID: ");
        String eno = scanner.nextLine();

        EMP emp = service.findEmployeeById(eno);
        if (emp != null) {
            System.out.println("\n=== Employee Found ===");
            System.out.println("ENO:   " + emp.getENO());
            System.out.println("ENAME: " + emp.getName());
            System.out.println("TITLE: " + emp.getTitle());
        } else {
            System.out.println("Employee not found");
        }
    }

    private static void addEmployee() throws Exception {
        System.out.print("Enter Employee ID: ");
        String eno = scanner.nextLine();
        System.out.print("Enter Employee Name: ");
        String name = scanner.nextLine();
        System.out.print("Enter Title: ");
        String title = scanner.nextLine();

        int result = service.addNewEmployee(eno, name, title);
        if (result > 0) {
            System.out.println("✓ Employee added successfully");
        } else {
            System.out.println("✗ Failed to add employee");
        }
    }

    private static void updateEmployee() throws Exception {
        System.out.print("Enter Employee ID to update: ");
        String eno = scanner.nextLine();
        System.out.print("Enter new Name: ");
        String name = scanner.nextLine();
        System.out.print("Enter new Title: ");
        String title = scanner.nextLine();

        int result = service.updateEmployee(eno, name, title);
        if (result > 0) {
            System.out.println("✓ Employee updated successfully");
        } else {
            System.out.println("✗ Failed to update employee");
        }
    }

    private static void deleteEmployee() throws Exception {
        System.out.print("Enter Employee ID to delete: ");
        String eno = scanner.nextLine();

        int result = service.deleteEmployee(eno);
        if (result > 0) {
            System.out.println("✓ Employee deleted successfully");
        } else {
            System.out.println("✗ Failed to delete employee");
        }
    }
}
