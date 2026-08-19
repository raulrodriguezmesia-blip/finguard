package com.ai.fintech.observability.infrastructure.persistence;

import com.ai.fintech.observability.application.port.TransactionRepository;
import com.ai.fintech.observability.domain.model.Transaction;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidad JPA que representa un resultado de evaluación de fraude.
 */
@Entity
@Table(name = "fraud_results")
public class FraudResultEntity {
    @Id
    private String transactionId;
    private BigDecimal riskScore;
    private String status;

    // Constructores
    public FraudResultEntity() {}

    public FraudResultEntity(com.ai.fintech.observability.domain.model.FraudResult fraudResult) {
        this.transactionId = fraudResult.transactionId().toString();
        this.riskScore = fraudResult.riskScore();
        this.status = fraudResult.status();
    }

    // Getters and Setters
    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public BigDecimal getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(BigDecimal riskScore) {
        this.riskScore = riskScore;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Convierte esta entidad a un objeto de dominio FraudResult.
     *
     * @return objeto de dominio FraudResult
     */
    public com.ai.fintech.observability.domain.model.FraudResult toDomainModel() {
        return new com.ai.fintech.observability.domain.model.FraudResult(
            UUID.fromString(transactionId),
            riskScore,
            status
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FraudResultEntity that = (FraudResultEntity) o;
        return Objects.equals(transactionId, that.transactionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transactionId);
    }
}