package com.ai.fintech.observability.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidad que representa una transacción financiera.
 */
public record Transaction(
        UUID id,
        CustomerId customerId,
        Money amount,
        String currency,
        LocalDateTime timestamp,
        String merchantCode
) {
    public Transaction {
        Objects.requireNonNull(id, "Transaction ID cannot be null");
        Objects.requireNonNull(customerId, "Customer ID cannot be null");
        Objects.requireNonNull(amount, "Amount cannot be null");
        Objects.requireNonNull(currency, "Currency cannot be null");
        Objects.requireNonNull(timestamp, "Timestamp cannot be null");
        Objects.requireNonNull(merchantCode, "Merchant code cannot be null");
    }
}