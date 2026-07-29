package com.example.finguard.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Resultado de la evaluación de fraude para una transacción.
 */
public class FraudResult {
    private String transactionId;
    private boolean isFraud;
    private BigDecimal fraudScore; // Score entre 0 y 1
    private String riskLevel; // LOW, MEDIUM, HIGH, CRITICAL
    private LocalDateTime evaluationTimestamp;
    private Map<String, Object> featuresUsed; // Características utilizadas para la decisión
    private String modelVersion; // Versión del modelo de ML utilizada

    // Constructores, getters y setters
    public FraudResult() {}

    public FraudResult(String transactionId, boolean isFraud, BigDecimal fraudScore, 
                      String riskLevel, LocalDateTime evaluationTimestamp,
                      Map<String, Object> featuresUsed, String modelVersion) {
        this.transactionId = transactionId;
        this.isFraud = isFraud;
        this.fraudScore = fraudScore;
        this.riskLevel = riskLevel;
        this.evaluationTimestamp = evaluationTimestamp;
        this.featuresUsed = featuresUsed;
        this.modelVersion = modelVersion;
    }

    // Getters y Setters
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    
    public boolean isFraud() { return isFraud; }
    public void setFraud(boolean fraud) { isFraud = fraud; }
    
    public BigDecimal getFraudScore() { return fraudScore; }
    public void setFraudScore(BigDecimal fraudScore) { this.fraudScore = fraudScore; }
    
    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    
    public LocalDateTime getEvaluationTimestamp() { return evaluationTimestamp; }
    public void setEvaluationTimestamp(LocalDateTime evaluationTimestamp) { this.evaluationTimestamp = evaluationTimestamp; }
    
    public Map<String, Object> getFeaturesUsed() { return featuresUsed; }
    public void setFeaturesUsed(Map<String, Object> featuresUsed) { this.featuresUsed = featuresUsed; }
    
    public String getModelVersion() { return modelVersion; }
    public void setModelVersion(String modelVersion) { this.modelVersion = modelVersion; }
}