package com.example;

import java.io.Serializable;
import java.time.Instant;

public class Payment implements Serializable {
    private static final long serialVersionUID = 1L;

    private String paymentId;
    private String portfolioId;
    private double amount;
    private PaymentType paymentType;
    private PaymentStatus status;
    private Instant createdAt;
    private Instant updatedAt;

    public enum PaymentType {
        DEPOSIT, WITHDRAWAL, DIVIDEND, FEE
    }

    public enum PaymentStatus {
        PENDING, COMPLETED, FAILED, CANCELLED
    }

    public Payment() {
    }

    public Payment(String paymentId, String portfolioId, double amount, PaymentType paymentType) {
        this.paymentId = paymentId;
        this.portfolioId = portfolioId;
        this.amount = amount;
        this.paymentType = paymentType;
        this.status = PaymentStatus.PENDING;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getPortfolioId() {
        return portfolioId;
    }

    public void setPortfolioId(String portfolioId) {
        this.portfolioId = portfolioId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public PaymentType getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(PaymentType paymentType) {
        this.paymentType = paymentType;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return paymentId + " | " + portfolioId + " | " + amount + " | " + paymentType + " | " + status;
    }
}
