package com.ai.fintech.observability.application.dtos;

import java.math.BigDecimal;

/**
 * DTO para responder con métricas de fraude.
 */
public class FraudMetricsDTO {
    private long totalTransactions;
    private long approvedTransactions;
    private long reviewTransactions;
    private BigDecimal averageRiskScore;

    // Getters and Setters
    public long getTotalTransactions() {
        return totalTransactions;
    }

    public void setTotalTransactions(long totalTransactions) {
        this.totalTransactions = totalTransactions;
    }

    public long getApprovedTransactions() {
        return approvedTransactions;
    }

    public void setApprovedTransactions(long approvedTransactions) {
        this.approvedTransactions = approvedTransactions;
    }

    public long getReviewTransactions() {
        return reviewTransactions;
    }

    public void setReviewTransactions(long reviewTransactions) {
        this.reviewTransactions = reviewTransactions;
    }

    public BigDecimal getAverageRiskScore() {
        return averageRiskScore;
    }

    public void setAverageRiskScore(BigDecimal averageRiskScore) {
        this.averageRiskScore = averageRiskScore;
    }
}