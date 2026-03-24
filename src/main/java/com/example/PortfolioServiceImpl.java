package com.example;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.UUID;

public class PortfolioServiceImpl extends UnicastRemoteObject implements PortfolioServiceRemote {
    private static final long serialVersionUID = 1L;
    private final PortfolioService portfolioService;

    public PortfolioServiceImpl() throws RemoteException {
        super();
        this.portfolioService = new PortfolioService();
    }

    @Override
    public String login(String username, String password) throws RemoteException {
        String requestId = generateRequestId();
        PortfolioService.ServiceResult<String> result = portfolioService.login(username, password, requestId);
        
        if (result.isSuccess()) {
            return result.getData();
        }
        throw new RemoteException("Login failed: " + result.getErrorCode());
    }

    @Override
    public List<Portfolio> getPortfolios(String userId, String role, String requestId) throws RemoteException {
        PortfolioService.ServiceResult<List<Portfolio>> result = portfolioService.getPortfolios(userId, role, requestId);
        
        if (result.isSuccess()) {
            return result.getData();
        }
        throw new RemoteException("Get portfolios failed: " + result.getErrorCode());
    }

    @Override
    public Portfolio createPortfolio(String userId, String role, String portfolioId, String name, String requestId) throws RemoteException {
        PortfolioService.ServiceResult<Portfolio> result = portfolioService.createPortfolio(userId, role, requestId, portfolioId, name);
        
        if (result.isSuccess()) {
            return result.getData();
        }
        throw new RemoteException("Create portfolio failed: " + result.getErrorCode());
    }

    @Override
    public Portfolio getPortfolio(String userId, String role, String portfolioId, String requestId) throws RemoteException {
        PortfolioService.ServiceResult<Portfolio> result = portfolioService.getPortfolio(userId, role, requestId, portfolioId);
        
        if (result.isSuccess()) {
            return result.getData();
        }
        throw new RemoteException("Get portfolio failed: " + result.getErrorCode());
    }

    @Override
    public Portfolio updatePortfolio(String userId, String role, String portfolioId, String name, String requestId) throws RemoteException {
        PortfolioService.ServiceResult<Portfolio> result = portfolioService.updatePortfolio(userId, role, requestId, portfolioId, name);
        
        if (result.isSuccess()) {
            return result.getData();
        }
        throw new RemoteException("Update portfolio failed: " + result.getErrorCode());
    }

    @Override
    public boolean deletePortfolio(String userId, String role, String portfolioId, String requestId) throws RemoteException {
        PortfolioService.ServiceResult<Boolean> result = portfolioService.deletePortfolio(userId, role, requestId, portfolioId);
        
        if (result.isSuccess()) {
            return result.getData();
        }
        throw new RemoteException("Delete portfolio failed: " + result.getErrorCode());
    }

    @Override
    public Holding addHolding(String userId, String role, String holdingId, String portfolioId, String assetSymbol, double quantity, double avgPrice, String requestId) throws RemoteException {
        PortfolioService.ServiceResult<Holding> result = portfolioService.addHolding(userId, role, requestId, holdingId, portfolioId, assetSymbol, quantity, avgPrice);
        
        if (result.isSuccess()) {
            return result.getData();
        }
        throw new RemoteException("Add holding failed: " + result.getErrorCode());
    }

    @Override
    public List<Holding> getHoldings(String userId, String role, String portfolioId, String requestId) throws RemoteException {
        PortfolioService.ServiceResult<List<Holding>> result = portfolioService.getHoldings(userId, role, requestId, portfolioId);
        
        if (result.isSuccess()) {
            return result.getData();
        }
        throw new RemoteException("Get holdings failed: " + result.getErrorCode());
    }

    @Override
    public Payment createPayment(String userId, String role, String paymentId, String portfolioId, double amount, String paymentType, String requestId) throws RemoteException {
        Payment.PaymentType type = Payment.PaymentType.valueOf(paymentType);
        PortfolioService.ServiceResult<Payment> result = portfolioService.createPayment(userId, role, requestId, paymentId, portfolioId, amount, type);
        
        if (result.isSuccess()) {
            return result.getData();
        }
        throw new RemoteException("Create payment failed: " + result.getErrorCode());
    }

    @Override
    public List<Payment> getPayments(String userId, String role, String portfolioId, String requestId) throws RemoteException {
        PortfolioService.ServiceResult<List<Payment>> result = portfolioService.getPayments(userId, role, requestId, portfolioId);
        
        if (result.isSuccess()) {
            return result.getData();
        }
        throw new RemoteException("Get payments failed: " + result.getErrorCode());
    }

    @Override
    public List<AuditEvent> getAuditLogs(String userId, String role, String targetType, String targetId, String requestId) throws RemoteException {
        PortfolioService.ServiceResult<List<AuditEvent>> result = portfolioService.getAuditLogs(userId, role, requestId, targetType, targetId);
        
        if (result.isSuccess()) {
            return result.getData();
        }
        throw new RemoteException("Get audit logs failed: " + result.getErrorCode());
    }
}
