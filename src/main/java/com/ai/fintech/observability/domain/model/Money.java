package com.ai.fintech.observability.domain.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Value Object que representa una cantidad monetaria.
 */
public record Money(BigDecimal amount, String currency) {
    public Money {
        Objects.requireNonNull(amount, "Amount cannot be null");
        Objects.requireNonNull(currency, "Currency cannot be null");
        
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
        
        // Validación básica de código de moneda ISO 4217
        if (currency == null || currency.length() != 3) {
            throw new IllegalArgumentException("Currency must be a valid ISO 4217 code");
        }
    }
    
    /**
     * Compara si esta cantidad es mayor que otra.
     * Asume misma moneda para simplificación.
     */
    public boolean greaterThan(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Cannot compare different currencies");
        }
        return this.amount.compareTo(other.amount) > 0;
    }
    
    /**
     * Crea una instancia de Money desde un double y código de moneda.
     */
    public static Money of(double amount, String currency) {
        return new Money(BigDecimal.valueOf(amount), currency);
    }
}