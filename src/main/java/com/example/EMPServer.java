package com.example;

import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;

public class EMPServer {
    public static void main(String[] args) {
        try {
            // Create RMI Registry
            Registry registry = LocateRegistry.createRegistry(1099);
            System.out.println("✓ RMI Registry started on port 1099");

            // Create and bind remote object
            EMPServiceImpl service = new EMPServiceImpl();
            registry.rebind("EMPService", service);
            System.out.println("✓ EMPService bound to registry");

            System.out.println("✓ Server is ready...");
            System.out.println("✓ Press Ctrl+C to stop the server");

            // Keep server running
            Thread.currentThread().join();

        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
