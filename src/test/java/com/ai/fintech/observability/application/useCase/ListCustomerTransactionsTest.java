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
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the ListCustomerTransactions use case.
 */
@ExtendWith(MockitoExtension.class)
class ListCustomerTransactionsTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private ListCustomerTransactions listCustomerTransactions;

    private String testCustomerId;
    private Transaction transaction1;
    private Transaction transaction2;

    @BeforeEach
    void setUp() {
        testCustomerId = UUID.randomUUID().toString();
        CustomerId customerId = new CustomerId(UUID.fromString(testCustomerId));

        // Create test transactions
        transaction1 = new Transaction(
            UUID.randomUUID(),
            customerId,
            new Money(BigDecimal.valueOf(100.0), "USD"),
            "USD",
            LocalDateTime.of(2026, 8, 18, 10, 0),
            "5411"
        );

        transaction2 = new Transaction(
            UUID.randomUUID(),
            customerId,
            new Money(BigDecimal.valueOf(200.0), "USD"),
            "USD",
            LocalDateTime.of(2026, 8, 18, 14, 30),
            "5411"
        );
    }

    @Test
    void shouldReturnListOfCustomerTransactions() {
        // Arrange
        when(transactionRepository.findByCustomerId(testCustomerId))
                .thenReturn(Arrays.asList(transaction1, transaction2));

        // Act
        List<Transaction> actualTransactions = listCustomerTransactions.execute(testCustomerId);

        // Assert
        assertNotNull(actualTransactions);
        assertEquals(2, actualTransactions.size());
        assertTrue(actualTransactions.contains(transaction1));
        assertTrue(actualTransactions.contains(transaction2));

        // Verify
        verify(transactionRepository).findByCustomerId(testCustomerId);
    }

    @Test
    void shouldReturnEmptyListWhenCustomerHasNoTransactions() {
        // Arrange
        when(transactionRepository.findByCustomerId(testCustomerId))
                .thenReturn(java.util.Collections.emptyList());

        // Act
        List<Transaction> actualTransactions = listCustomerTransactions.execute(testCustomerId);

        // Assert
        assertNotNull(actualTransactions);
        assertTrue(actualTransactions.isEmpty());

        // Verify
        verify(transactionRepository).findByCustomerId(testCustomerId);
    }
}