package com.ai.fintech.observability.infrastructure.persistence;

import com.ai.fintech.observability.application.port.TransactionRepository;
import com.ai.fintech.observability.domain.model.Transaction;
import com.ai.fintech.observability.domain.model.CustomerId;
import com.ai.fintech.observability.domain.model.Money;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidad JPA que representa una transacción financiera.
 */
@Entity
@Table(name = "transactions")
public class TransactionEntity {
    @Id
    private String id;
    private String customerId;
    private BigDecimal amount;
    private String currency;
    private LocalDateTime timestamp;
    private String merchantCode;

    // Constructores
    public TransactionEntity() {}

    public TransactionEntity(Transaction transaction) {
        this.id = transaction.id().toString();
        this.customerId = transaction.customerId().value().toString();
        this.amount = transaction.amount().amount();
        this.currency = transaction.amount().currency();
        this.timestamp = transaction.timestamp();
        this.merchantCode = transaction.merchantCode();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getMerchantCode() {
        return merchantCode;
    }

    public void setMerchantCode(String merchantCode) {
        this.merchantCode = merchantCode;
    }

    /**
     * Convierte esta entidad a un objeto de dominio Transaction.
     *
     * @return objeto de dominio Transaction
     */
    public Transaction toDomainModel() {
        return new Transaction(
            UUID.fromString(id),
            new CustomerId(UUID.fromString(customerId)),
            new Money(amount, currency),
            currency,
            timestamp,
            merchantCode
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransactionEntity that = (TransactionEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}