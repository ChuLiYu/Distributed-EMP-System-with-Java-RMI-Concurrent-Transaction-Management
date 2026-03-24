package com.example;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.UUID;

public interface PortfolioServiceRemote extends Remote {
    
    String login(String username, String password) throws RemoteException;
    
    List<Portfolio> getPortfolios(String userId, String role, String requestId) throws RemoteException;
    
    Portfolio createPortfolio(String userId, String role, String portfolioId, String name, String requestId) throws RemoteException;
    
    Portfolio getPortfolio(String userId, String role, String portfolioId, String requestId) throws RemoteException;
    
    Portfolio updatePortfolio(String userId, String role, String portfolioId, String name, String requestId) throws RemoteException;
    
    boolean deletePortfolio(String userId, String role, String portfolioId, String requestId) throws RemoteException;
    
    Holding addHolding(String userId, String role, String holdingId, String portfolioId, String assetSymbol, double quantity, double avgPrice, String requestId) throws RemoteException;
    
    List<Holding> getHoldings(String userId, String role, String portfolioId, String requestId) throws RemoteException;
    
    Payment createPayment(String userId, String role, String paymentId, String portfolioId, double amount, String paymentType, String requestId) throws RemoteException;
    
    List<Payment> getPayments(String userId, String role, String portfolioId, String requestId) throws RemoteException;
    
    List<AuditEvent> getAuditLogs(String userId, String role, String targetType, String targetId, String requestId) throws RemoteException;
    
    default String generateRequestId() {
        return UUID.randomUUID().toString();
    }
}
