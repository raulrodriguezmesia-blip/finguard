package com.example.finguard;

import com.example.finguard.adapters.inbound.TransactionRestAdapter.CustomerEventRequest;
import com.example.finguard.adapters.inbound.TransactionRestAdapter.TransactionRequest;
import com.example.finguard.domain.FraudResult;
import com.example.finguard.domain.Transaction;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Prueba de integración básica para verificar que el flujo completo funciona.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FinguardApplicationTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void contextLoads() {
    }

    @Test
    void shouldEvaluateTransactionAndReturnFraudResult() {
        // Arrange
        TransactionRequest request = new TransactionRequest();
        request.setTransactionId("txn_001");
        request.setCustomerId("cust_123");
        request.setAmount(1500.0);
        request.setMerchantId("merch_456");
        request.setMerchantCategoryCode("7995"); // Juegos de azar - alto riesgo
        request.setTimestamp(LocalDateTime.now());
        request.setCurrency("USD");
        request.setPaymentMethod("credit_card");

        // Act
        ResponseEntity<FraudResult> response = restTemplate.exchange(
                "/api/v1/transactions/evaluate",
                HttpMethod.POST,
                new org.springframework.http.HttpEntity<>(request),
                FraudResult.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        FraudResult result = response.getBody();
        assertThat(result).isNotNull();
        assertThat(result.getTransactionId()).isEqualTo("txn_001");
        assertThat(result.isFraud()).isTrue(); // Debe ser fraude por monto alto y MCC de juegos
        assertThat(result.getFraudScore()).isGreaterThan(java.math.BigDecimal.valueOf(0.5));
    }

    @Test
    void shouldProcessLowRiskTransactionAsNotFraud() {
        // Arrange
        TransactionRequest request = new TransactionRequest();
        request.setTransactionId("txn_002");
        request.setCustomerId("cust_124");
        request.setAmount(25.0);
        request.setMerchantId("merch_789");
        request.setMerchantCategoryCode("5411"); // Supermercado - bajo riesgo
        request.setTimestamp(LocalDateTime.now().withHour(14));
        request.setCurrency("USD");
        request.setPaymentMethod("credit_card");

        // Act
        ResponseEntity<FraudResult> response = restTemplate.exchange(
                "/api/v1/transactions/evaluate",
                HttpMethod.POST,
                new org.springframework.http.HttpEntity<>(request),
                FraudResult.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        FraudResult result = response.getBody();
        assertThat(result).isNotNull();
        assertThat(result.getTransactionId()).isEqualTo("txn_002");
        assertThat(result.isFraud()).isFalse(); // No debería ser fraude
        assertThat(result.getFraudScore()).isLessThan(java.math.BigDecimal.valueOf(0.7));
    }

    @Test
    void shouldUpdateCustomerFeatures() {
        // Arrange
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("loginAttempt", true);
        eventData.put("ipAddress", "192.168.1.1");

        CustomerEventRequest request = new CustomerEventRequest();
        request.setCustomerId("cust_125");
        request.setEventType("login");
        request.setEventData(eventData);

        // Act
        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/v1/transactions/customers/events",
                HttpMethod.POST,
                new org.springframework.http.HttpEntity<>(request),
                Void.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
    }
}