package com.ai.fintech.observability.integration;

import com.ai.fintech.observability.application.port.TransactionRepository;
import com.ai.fintech.observability.application.port.FraudResultRepository;
import com.ai.fintech.observability.domain.model.Transaction;
import com.ai.fintech.observability.domain.model.CustomerId;
import com.ai.fintech.observability.domain.model.Money;
import com.ai.fintech.observability.domain.model.FraudResult;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for repository implementations using Testcontainers.
 * Tests persistence of transactions and fraud results, and validates metrics aggregation.
 */
@Testcontainers
@ExtendWith(SpringExtension.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
        com.ai.fintech.observability.infrastructure.persistence.TransactionRepositoryImpl.class,
        com.ai.fintech.observability.infrastructure.persistence.FraudResultRepositoryImpl.class
})
class RepositoryIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRESQL_CONTAINER = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("observability_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void postgresqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRESQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRESQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", POSTGRESQL_CONTAINER::getPassword);
    }

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private FraudResultRepository fraudResultRepository;

    private CustomerId testCustomerId;

    @BeforeEach
    void setUp() {
        testCustomerId = new CustomerId(UUID.randomUUID());
    }

    @Test
    void shouldPersistTransactionAndFraudResult() {
        // Arrange
        Transaction transaction = new Transaction(
            UUID.randomUUID(),
            testCustomerId,
            new Money(BigDecimal.valueOf(1500.0), "USD"),
            "USD",
            LocalDateTime.of(2026, 8, 18, 20, 30), // Outside business hours
            "5411"
        );

        // Act
        Transaction savedTransaction = transactionRepository.save(transaction);
        com.ai.fintech.observability.domain.model.FraudResult fraudResult = 
                new com.ai.fintech.observability.domain.service.impl.FraudDetectionServiceImpl()
                        .evaluateTransaction(savedTransaction);
        FraudResult savedFraudResult = fraudResultRepository.save(fraudResult);

        // Assert
        assertNotNull(savedTransaction.id());
        assertNotNull(savedFraudResult.transactionId());
        
        // Verify transaction was persisted correctly
        Transaction foundTransaction = transactionRepository.findById(savedTransaction.id().toString());
        assertNotNull(foundTransaction);
        assertEquals(savedTransaction.id(), foundTransaction.id());
        assertEquals(savedTransaction.customerId(), foundTransaction.customerId());
        assertEquals(savedTransaction.amount(), foundTransaction.amount());
        assertEquals(savedTransaction.currency(), foundTransaction.currency());
        assertEquals(savedTransaction.timestamp(), foundTransaction.timestamp());
        assertEquals(savedTransaction.merchantCode(), foundTransaction.merchantCode());

        // Verify fraud result was persisted correctly
        FraudResult foundFraudResult = fraudResultRepository.findByTransactionId(savedFraudResult.transactionId().toString());
        assertNotNull(foundFraudResult);
        assertEquals(savedFraudResult.transactionId(), foundFraudResult.transactionId());
        assertEquals(savedFraudResult.riskScore(), foundFraudResult.riskScore());
        assertEquals(savedFraudResult.status(), foundFraudResult.status());
    }

    @Test
    void shouldListCustomerTransactionsCorrectly() {
        // Arrange
        Transaction transaction1 = new Transaction(
            UUID.randomUUID(),
            testCustomerId,
            new Money(BigDecimal.valueOf(100.0), "USD"),
            "USD",
            LocalDateTime.of(2026, 8, 18, 10, 0), // Within business hours
            "5411"
        );

        Transaction transaction2 = new Transaction(
            UUID.randomUUID(),
            testCustomerId,
            new Money(BigDecimal.valueOf(2000.0), "USD"), // High amount
            "USD",
            LocalDateTime.of(2026, 8, 18, 22, 0), // Outside business hours
            "5411"
        );

        // Act
        transactionRepository.save(transaction1);
        transactionRepository.save(transaction2);
        
        List<Transaction> customerTransactions = transactionRepository.findByCustomerId(testCustomerId.value().toString());

        // Assert
        assertNotNull(customerTransactions);
        assertEquals(2, customerTransactions.size());
        assertTrue(customerTransactions.contains(transaction1));
        assertTrue(customerTransactions.contains(transaction2));
    }

    @Test
    void shouldCalculateFraudMetricsCorrectly() {
        // Arrange
        // Create approved transaction (low amount, business hours)
        Transaction approvedTx = new Transaction(
            UUID.randomUUID(),
            testCustomerId,
            new Money(BigDecimal.valueOf(50.0), "USD"),
            "USD",
            LocalDateTime.of(2026, 8, 18, 14, 0), // 2:00 PM
            "5411"
        );
        
        // Create review transaction (high amount)
        Transaction reviewTx1 = new Transaction(
            UUID.randomUUID(),
            testCustomerId,
            new Money(BigDecimal.valueOf(1500.0), "USD"),
            "USD",
            LocalDateTime.of(2026, 8, 18, 16, 0), // 4:00 PM
            "5411"
        );
        
        // Create review transaction (outside business hours)
        Transaction reviewTx2 = new Transaction(
            UUID.randomUUID(),
            testCustomerId,
            new Money(BigDecimal.valueOf(75.0), "USD"),
            "USD",
            LocalDateTime.of(2026, 8, 18, 20, 0), // 8:00 PM
            "5411"
        );

        // Act
        transactionRepository.save(approvedTx);
        transactionRepository.save(reviewTx1);
        transactionRepository.save(reviewTx2);
        
        // Evaluate and save fraud results
        com.ai.fintech.observability.domain.service.impl.FraudDetectionServiceImpl fraudService = 
                new com.ai.fintech.observability.domain.service.impl.FraudDetectionServiceImpl();
        
        fraudResultRepository.save(fraudService.evaluateTransaction(approvedTx));
        fraudResultRepository.save(fraudService.evaluateTransaction(reviewTx1));
        fraudResultRepository.save(fraudService.evaluateTransaction(reviewTx2));
        
        // Get metrics
        com.ai.fintech.observability.application.port.FraudMetrics metrics = fraudResultRepository.getFraudMetrics();

        // Assert
        assertNotNull(metrics);
        assertEquals(3L, metrics.getTotalTransactions(), "Should have 3 total transactions");
        assertEquals(1L, metrics.getApprovedTransactions(), "Should have 1 approved transaction");
        assertEquals(2L, metrics.getReviewTransactions(), "Should have 2 review transactions");
        
        // Expected average: (0.2 + 0.8 + 0.8) / 3 = 0.6
        BigDecimal expectedAverage = new BigDecimal("0.6");
        assertEquals(expectedAverage, metrics.getAverageRiskScore(), 
                "Average risk score should be 0.6");
    }

    @Test
    void shouldHandleEmptyStateMetrics() {
        // Act
        com.ai.fintech.observability.application.port.FraudMetrics metrics = fraudResultRepository.getFraudMetrics();

        // Assert
        assertNotNull(metrics);
        assertEquals(0L, metrics.getTotalTransactions());
        assertEquals(0L, metrics.getApprovedTransactions());
        assertEquals(0L, metrics.getReviewTransactions());
        assertEquals(BigDecimal.ZERO, metrics.getAverageRiskScore());
    }
}
