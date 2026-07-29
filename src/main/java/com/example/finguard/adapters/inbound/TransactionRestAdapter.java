package com.example.finguard.adapters.inbound;

import com.example.finguard.domain.FraudDetectionService;
import com.example.finguard.domain.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Adaptador de entrada que expone una API REST para evaluar transacciones.
 * Implementa el puerto de entrada TransactionInPort.
 */
@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionRestAdapter {

    private final FraudDetectionService fraudDetectionService;

    @Autowired
    public TransactionRestAdapter(FraudDetectionService fraudDetectionService) {
        this.fraudDetectionService = fraudDetectionService;
    }

    /**
     * Endpoint para evaluar una transacción y determinar si es fraudulenta.
     */
    @PostMapping("/evaluate")
    public ResponseEntity<FraudResult> evaluateTransaction(@Valid @RequestBody TransactionRequest request) {
        // Convertir el request de REST a objeto de dominio
        Transaction transaction = new Transaction(
                request.getTransactionId(),
                request.getCustomerId(),
                request.getMerchantId(),
                java.math.BigDecimal.valueOf(request.getAmount()),
                request.getCurrency(),
                request.getTimestamp(),
                request.getMerchantCategoryCode(),
                request.getPaymentMethod()
        );

        // Procesar mediante el servicio de dominio
        FraudResult result = fraudDetectionService.evaluateFraud(transaction);

        return ResponseEntity.ok(result);
    }

    /**
     * Endpoint para recibir eventos de clientes y actualizar sus características.
     * Útil para mantener actualizado el feature store en tiempo real.
     */
    @PostMapping("/customers/events")
    public ResponseEntity<Void> processCustomerEvent(@Valid @RequestBody CustomerEventRequest request) {
        CustomerEvent event = new CustomerEvent(
                request.getCustomerId(),
                request.getEventType(),
                request.getEventData(),
                System.currentTimeMillis()
        );

        fraudDetectionService.updateCustomerFeatures(event);
        return ResponseEntity.accepted().build();
    }

    // Clases DTO para la API REST
    public static class TransactionRequest {
        @NotBlank(message = "transactionId es obligatorio")
        private String transactionId;

        @NotBlank(message = "customerId es obligatorio")
        private String customerId;

        @NotNull(message = "amount es obligatorio")
        private Double amount;

        @NotBlank(message = "merchantId es obligatorio")
        private String merchantId;

        @NotBlank(message = "merchantCategoryCode es obligatorio")
        private String merchantCategoryCode;

        @NotNull(message = "timestamp es obligatorio")
        private LocalDateTime timestamp;

        @NotBlank(message = "currency es obligatorio")
        private String currency;

        @NotBlank(message = "paymentMethod es obligatorio")
        private String paymentMethod;

        public String getTransactionId() { return transactionId; }
        public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

        public String getCustomerId() { return customerId; }
        public void setCustomerId(String customerId) { this.customerId = customerId; }

        public Double getAmount() { return amount; }
        public void setAmount(Double amount) { this.amount = amount; }

        public String getMerchantId() { return merchantId; }
        public void setMerchantId(String merchantId) { this.merchantId = merchantId; }

        public String getMerchantCategoryCode() { return merchantCategoryCode; }
        public void setMerchantCategoryCode(String merchantCategoryCode) { this.merchantCategoryCode = merchantCategoryCode; }

        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }

        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    }

    public static class CustomerEventRequest {
        @NotBlank(message = "customerId es obligatorio")
        private String customerId;

        @NotBlank(message = "eventType es obligatorio")
        private String eventType;

        @NotNull(message = "eventData es obligatorio")
        private Object eventData;

        public String getCustomerId() { return customerId; }
        public void setCustomerId(String customerId) { this.customerId = customerId; }

        public String getEventType() { return eventType; }
        public void setEventType(String eventType) { this.eventType = eventType; }

        public Object getEventData() { return eventData; }
        public void setEventData(Object eventData) { this.eventData = eventData; }
    }
}

