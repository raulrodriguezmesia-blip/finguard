package com.ai.fintech.observability.application.useCase;

import com.ai.fintech.observability.application.port.TransactionRepository;
import com.ai.fintech.observability.application.port.FraudResultRepository;
import com.ai.fintech.observability.domain.model.Transaction;
import com.ai.fintech.observability.domain.model.CustomerId;
import com.ai.fintech.observability.domain.model.Money;
import com.ai.fintech.observability.domain.model.FraudResult;
import com.ai.fintech.observability.domain.service.FraudDetectionService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the RegisterTransaction use case.
 */
@ExtendWith(MockitoExtension.class)
class RegisterTransactionTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private FraudDetectionService fraudDetectionService;

    @Mock
    private FraudResultRepository fraudResultRepository;

    @InjectMocks
    private RegisterTransaction registerTransaction;

    private Transaction testTransaction;
    private FraudResult expectedFraudResult;

    @BeforeEach
    void setUp() {
        // Create a test transaction
        testTransaction = new Transaction(
            UUID.randomUUID(),
            new CustomerId(UUID.randomUUID()),
            new Money(BigDecimal.valueOf(1500.0), "USD"),
            "USD",
            LocalDateTime.now(),
            "5411"
        );

        // Create expected fraud result
        expectedFraudResult = new FraudResult(
            testTransaction.id(),
            new BigDecimal("0.8"),
            "REVIEW"
        );
    }

    @Test
    void shouldRegisterTransactionAndEvaluateForFraud() {
        // Arrange
        when(transactionRepository.save(testTransaction)).thenReturn(testTransaction);
        when(fraudDetectionService.evaluateTransaction(testTransaction)).thenReturn(expectedFraudResult);
        when(fraudResultRepository.save(expectedFraudResult)).thenReturn(expectedFraudResult);

        // Act
        FraudResult actualResult = registerTransaction.execute(testTransaction);

        // Assert
        assertNotNull(actualResult);
        assertEquals(expectedFraudResult.transactionId(), actualResult.transactionId());
        assertEquals(expectedFraudResult.riskScore(), actualResult.riskScore());
        assertEquals(expectedFraudResult.status(), actualResult.status());

        // Verify interactions
        verify(transactionRepository).save(testTransaction);
        verify(fraudDetectionService).evaluateTransaction(testTransaction);
        verify(fraudResultRepository).save(expectedFraudResult);
    }

    @Test
    void shouldHandleLowAmountTransactionDuringBusinessHours() {
        // Arrange
        Transaction lowAmountTransaction = new Transaction(
            UUID.randomUUID(),
            new CustomerId(UUID.randomUUID()),
            new Money(BigDecimal.valueOf(50.0), "USD"),
            "USD",
            LocalDateTime.of(2026, 8, 18, 10, 0), // 10:00 AM - within business hours
            "5411"
        );

        FraudResult expectedResult = new FraudResult(
            lowAmountTransaction.id(),
            new BigDecimal("0.2"),
            "APPROVED"
        );

        when(transactionRepository.save(lowAmountTransaction)).thenReturn(lowAmountTransaction);
        when(fraudDetectionService.evaluateTransaction(lowAmountTransaction)).thenReturn(expectedResult);
        when(fraudResultRepository.save(expectedResult)).thenReturn(expectedResult);

        // Act
        FraudResult actualResult = registerTransaction.execute(lowAmountTransaction);

        // Assert
        assertNotNull(actualResult);
        assertEquals(expectedResult.riskScore(), actualResult.riskScore());
        assertEquals(expectedResult.status(), actualResult.status());
    }
}