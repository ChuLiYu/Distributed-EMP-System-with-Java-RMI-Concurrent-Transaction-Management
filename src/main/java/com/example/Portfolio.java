package com.example;

import java.io.Serializable;
import java.time.Instant;

public class Portfolio implements Serializable {
    private static final long serialVersionUID = 1L;

    private String portfolioId;
    private String userId;
    private String name;
    private Instant createdAt;
    private Instant updatedAt;

    public Portfolio() {
    }

    public Portfolio(String portfolioId, String userId, String name) {
        this.portfolioId = portfolioId;
        this.userId = userId;
        this.name = name;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public String getPortfolioId() {
        return portfolioId;
    }

    public void setPortfolioId(String portfolioId) {
        this.portfolioId = portfolioId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
        return portfolioId + " | " + userId + " | " + name;
    }
}
