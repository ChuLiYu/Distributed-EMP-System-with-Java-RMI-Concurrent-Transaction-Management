package com.example;

import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;

public class EMPServer {
    public static void main(String[] args) {
        try {
            System.out.println("=== FINTECH Server Starting ===\n");
            
            System.out.println("Initializing database schema...");
            DatabaseInitializer.initialize();
            System.out.println();
            
            System.out.println("Starting RMI Registry...");
            Registry registry = LocateRegistry.createRegistry(1099);
            System.out.println("✓ RMI Registry started on port 1099\n");

            System.out.println("Binding services...");
            
            EMPServiceImpl empService = new EMPServiceImpl();
            registry.rebind("EMPService", empService);
            System.out.println("✓ EMPService bound to registry");

            PortfolioServiceImpl portfolioService = new PortfolioServiceImpl();
            registry.rebind("PortfolioService", portfolioService);
            System.out.println("✓ PortfolioService bound to registry");

            System.out.println("\n=== Server Ready ===");
            System.out.println("Available services:");
            System.out.println("  - EMPService: Legacy employee management");
            System.out.println("  - PortfolioService: Financial portfolio management (RBAC enabled)");
            System.out.println("\nDefault users (username/password):");
            System.out.println("  - admin/admin123 (Admin - full access)");
            System.out.println("  - supervisor/super123 (Supervisor - full access except audit)");
            System.out.println("  - operator/operator123 (Operator - CRUD without delete)");
            System.out.println("  - viewer/viewer123 (Viewer - read only)");
            System.out.println("\nPress Ctrl+C to stop the server");

            Thread.currentThread().join();

        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
