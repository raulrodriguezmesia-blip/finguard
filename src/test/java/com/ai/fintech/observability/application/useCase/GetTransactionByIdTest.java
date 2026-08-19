package com.ai.fintech.observability.application.useCase;

import com.ai.fintech.observability.application.port.TransactionRepository;
import com.ai.fintech.observability.domain.model.Transaction;
import com.ai.fintech.observability.domain.model.CustomerId;
import com.ai.fintech.observability.domain.model.Money;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the GetTransactionById use case.
 */
@ExtendWith(MockitoExtension.class)
class GetTransactionByIdTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private GetTransactionById getTransactionById;

    private Transaction testTransaction;

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
    }

    @Test
    void shouldReturnTransactionWhenFound() {
        // Arrange
        String transactionId = testTransaction.id().toString();
        when(transactionRepository.findById(transactionId)).thenReturn(testTransaction);

        // Act
        Transaction actualTransaction = getTransactionById.execute(transactionId);

        // Assert
        assertNotNull(actualTransaction);
        assertEquals(testTransaction.id(), actualTransaction.id());
        assertEquals(testTransaction.customerId(), actualTransaction.customerId());
        assertEquals(testTransaction.amount(), actualTransaction.amount());
        assertEquals(testTransaction.currency(), actualTransaction.currency());
        assertEquals(testTransaction.timestamp(), actualTransaction.timestamp());
        assertEquals(testTransaction.merchantCode(), actualTransaction.merchantCode());

        // Verify
        verify(transactionRepository).findById(transactionId);
    }

    @Test
    void shouldReturnNullWhenTransactionNotFound() {
        // Arrange
        String nonExistentId = UUID.randomUUID().toString();
        when(transactionRepository.findById(nonExistentId)).thenReturn(null);

        // Act
        Transaction actualTransaction = getTransactionById.execute(nonExistentId);

        // Assert
        assertNull(actualTransaction);

        // Verify
        verify(transactionRepository).findById(nonExistentId);
    }
}