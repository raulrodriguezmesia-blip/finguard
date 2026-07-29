package com.example.finguard.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Representa una transacción financiera.
 */
public class Transaction {
    private String transactionId;
    private String customerId;
    private String merchantId;
    private BigDecimal amount;
    private String currency;
    private LocalDateTime timestamp;
    private String merchantCategoryCode;
    private String paymentMethod;
    // Otros campos relevantes para detección de fraude

    // Constructores, getters y setters
    public Transaction() {}

    public Transaction(String transactionId, String customerId, String merchantId, 
                      BigDecimal amount, String currency, LocalDateTime timestamp, 
                      String merchantCategoryCode, String paymentMethod) {
        this.transactionId = transactionId;
        this.customerId = customerId;
        this.merchantId = merchantId;
        this.amount = amount;
        this.currency = currency;
        this.timestamp = timestamp;
        this.merchantCategoryCode = merchantCategoryCode;
        this.paymentMethod = paymentMethod;
    }

    // Getters y Setters
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    
    public String getMerchantId() { return merchantId; }
    public void setMerchantId(String merchantId) { this.merchantId = merchantId; }
    
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    public String getMerchantCategoryCode() { return merchantCategoryCode; }
    public void setMerchantCategoryCode(String merchantCategoryCode) { this.merchantCategoryCode = merchantCategoryCode; }
    
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
}