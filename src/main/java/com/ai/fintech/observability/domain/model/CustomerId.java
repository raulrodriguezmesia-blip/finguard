package com.ai.fintech.observability.domain.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object que representa un identificador de cliente.
 */
public record CustomerId(UUID value) {
    public CustomerId {
        Objects.requireNonNull(value, "Customer ID value cannot be null");
    }
    
    /**
     * Crea una instancia de CustomerId desde un string.
     */
    public static CustomerId fromString(String id) {
        return new CustomerId(UUID.fromString(id));
    }
    
    @Override
    public String toString() {
        return value.toString();
    }
}