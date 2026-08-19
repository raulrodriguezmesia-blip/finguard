package com.ai.fintech.observability.application.port;

import java.math.BigDecimal;

/**
 * Clase de soporte para métricas de fraude.
 */
public class FraudMetrics {
    private long totalTransactions;
    private long approvedTransactions;
    private long reviewTransactions;
    private BigDecimal averageRiskScore;

    public FraudMetrics() {
        this.totalTransactions = 0;
        this.approvedTransactions = 0;
        this.reviewTransactions = 0;
        this.averageRiskScore = BigDecimal.ZERO;
    }

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