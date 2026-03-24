package com.example;

import java.io.Serializable;
import java.time.Instant;

public class Holding implements Serializable {
    private static final long serialVersionUID = 1L;

    private String holdingId;
    private String portfolioId;
    private String assetSymbol;
    private double quantity;
    private double avgPrice;
    private Instant createdAt;
    private Instant updatedAt;

    public Holding() {
    }

    public Holding(String holdingId, String portfolioId, String assetSymbol, double quantity, double avgPrice) {
        this.holdingId = holdingId;
        this.portfolioId = portfolioId;
        this.assetSymbol = assetSymbol;
        this.quantity = quantity;
        this.avgPrice = avgPrice;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public String getHoldingId() {
        return holdingId;
    }

    public void setHoldingId(String holdingId) {
        this.holdingId = holdingId;
    }

    public String getPortfolioId() {
        return portfolioId;
    }

    public void setPortfolioId(String portfolioId) {
        this.portfolioId = portfolioId;
    }

    public String getAssetSymbol() {
        return assetSymbol;
    }

    public void setAssetSymbol(String assetSymbol) {
        this.assetSymbol = assetSymbol;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public double getAvgPrice() {
        return avgPrice;
    }

    public void setAvgPrice(double avgPrice) {
        this.avgPrice = avgPrice;
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
        return holdingId + " | " + portfolioId + " | " + assetSymbol + " | " + quantity + " @ " + avgPrice;
    }
}
