package com.ai.fintech.observability.infrastructure.web;

import com.ai.fintech.observability.application.dtos.TransactionRequestDTO;
import com.ai.fintech.observability.application.dtos.FraudResultDTO;
import com.ai.fintech.observability.application.dtos.FraudMetricsDTO;
import com.ai.fintech.observability.application.dtos.TransactionResponseDTO;
import com.ai.fintech.observability.application.useCase.RegisterTransaction;
import com.ai.fintech.observability.application.useCase.GetTransactionById;
import com.ai.fintech.observability.application.useCase.ListCustomerTransactions;
import com.ai.fintech.observability.application.useCase.GetFraudMetrics;
import com.ai.fintech.observability.domain.model.Transaction;
import com.ai.fintech.observability.domain.model.CustomerId;
import com.ai.fintech.observability.domain.model.Money;
import com.ai.fintech.observability.domain.model.FraudResult;
import com.ai.fintech.observability.application.port.FraudMetrics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.http.MediaType;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for the TransactionController.
 */
@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private RegisterTransaction registerTransaction;

    @Mock
    private GetTransactionById getTransactionById;

    @Mock
    private ListCustomerTransactions listCustomerTransactions;

    @Mock
    private GetFraudMetrics getFraudMetrics;

    @InjectMocks
    private TransactionController transactionController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mockMvc = MockMvcBuilders.standaloneSetup(transactionController)
                .setControllerAdvice(new com.ai.fintech.observability.infrastructure.config.GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldRegisterTransactionAndReturnFraudResult() throws Exception {
        // Arrange
        TransactionRequestDTO request = new TransactionRequestDTO();
        request.setCustomerId(UUID.randomUUID().toString());
        request.setAmount(new BigDecimal("1500.00"));
        request.setCurrency("USD");
        request.setTimestamp(LocalDateTime.now());
        request.setMerchantCode("5411");

        FraudResult expectedResult = new FraudResult(
            UUID.randomUUID(),
            new BigDecimal("0.8"),
            "REVIEW"
        );

        when(registerTransaction.execute(any(Transaction.class))).thenReturn(expectedResult);

        // Act & Assert
        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").isNotEmpty())
                .andExpect(jsonPath("$.riskScore").value(0.8))
                .andExpect(jsonPath("$.status").value("REVIEW"));

        // Verify
        verify(registerTransaction).execute(any(Transaction.class));
    }

    @Test
    void shouldReturnBadRequestWhenInvalidInput() throws Exception {
        // Arrange
        TransactionRequestDTO request = new TransactionRequestDTO();
        request.setCustomerId(""); // Invalid: empty customerId
        request.setAmount(new BigDecimal("-100.00")); // Invalid: negative amount
        request.setCurrency("US"); // Invalid: currency must be 3 characters
        request.setTimestamp(LocalDateTime.now());
        request.setMerchantCode("5411");

        // Act & Assert
        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.customerId").isNotEmpty())
                .andExpect(jsonPath("$.errors.amount").isNotEmpty())
                .andExpect(jsonPath("$.errors.currency").isNotEmpty());
    }

    @Test
    void shouldReturnNotFoundWhenTransactionDoesNotExist() throws Exception {
        // Arrange
        String nonExistentId = UUID.randomUUID().toString();
        when(getTransactionById.execute(nonExistentId)).thenReturn(null);

        // Act & Assert
        mockMvc.perform(get("/api/transactions/{id}", nonExistentId))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnListOfCustomerTransactions() throws Exception {
        // Arrange
        String customerId = UUID.randomUUID().toString();
        List<Transaction> transactions = new ArrayList<>();
        Transaction transaction = new Transaction(
            UUID.randomUUID(),
            new CustomerId(UUID.randomUUID()),
            new Money(new BigDecimal("100.00"), "USD"),
            "USD",
            LocalDateTime.now(),
            "5411"
        );
        transactions.add(transaction);

        when(listCustomerTransactions.execute(customerId)).thenReturn(transactions);

        // Act & Assert
        mockMvc.perform(get("/api/customers/{customerId}/transactions", customerId))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    void shouldReturnFraudMetrics() throws Exception {
        // Arrange
        FraudMetrics expectedMetrics = new FraudMetrics() {
            @Override
            public long getTotalTransactions() { return 100L; }
            @Override
            public long getApprovedTransactions() { return 80L; }
            @Override
            public long getReviewTransactions() { return 20L; }
            @Override
            public BigDecimal getAverageRiskScore() { return new BigDecimal("0.25"); }
        };

        when(getFraudMetrics.execute()).thenReturn(expectedMetrics);

        // Act & Assert
        mockMvc.perform(get("/api/metrics/fraud"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTransactions").value(100))
                .andExpect(jsonPath("$.approvedTransactions").value(80))
                .andExpect(jsonPath("$.reviewTransactions").value(20))
                .andExpect(jsonPath("$.averageRiskScore").value(0.25));
    }
}
