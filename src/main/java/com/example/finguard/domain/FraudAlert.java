package com.example.finguard.domain;

/**
 * Alerta de fraude que se envía cuando se detecta una transacción sospechosa.
 */
public class FraudAlert {
    private String transactionId;
    private String customerId;
    private Double fraudScore;
    private String reason;
    private long timestamp;

    // Constructores
    public FraudAlert() {}

    public FraudAlert(String transactionId, String customerId, Double fraudScore,
                     String reason, long timestamp) {
        this.transactionId = transactionId;
        this.customerId = customerId;
        this.fraudScore = fraudScore;
        this.reason = reason;
        this.timestamp = timestamp;
    }

    // Getters y Setters
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public Double getFraudScore() { return fraudScore; }
    public void setFraudScore(Double fraudScore) { this.fraudScore = fraudScore; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
