package com.ai.fintech.observability.domain.model;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidad que representa el resultado de la evaluación de fraude.
 */
public record FraudResult(
        UUID transactionId,
        BigDecimal riskScore,
        String status
) {
    public FraudResult {
        Objects.requireNonNull(transactionId, "Transaction ID cannot be null");
        Objects.requireNonNull(riskScore, "Risk score cannot be null");
        Objects.requireNonNull(status, "Status cannot be null");
    }
}