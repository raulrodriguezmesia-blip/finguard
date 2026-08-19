package com.ai.fintech.observability.application.useCase;

import com.ai.fintech.observability.application.port.FraudResultRepository;
import com.ai.fintech.observability.application.port.FraudMetrics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the GetFraudMetrics use case.
 */
@ExtendWith(MockitoExtension.class)
class GetFraudMetricsTest {

    @Mock
    private FraudResultRepository fraudResultRepository;

    @InjectMocks
    private GetFraudMetrics getFraudMetrics;

    private FraudMetrics expectedMetrics;

    @BeforeEach
    void setUp() {
        // Create expected metrics
        expectedMetrics = new FraudMetrics();
        expectedMetrics.setTotalTransactions(100L);
        expectedMetrics.setApprovedTransactions(80L);
        expectedMetrics.setReviewTransactions(20L);
        expectedMetrics.setAverageRiskScore(new BigDecimal("0.25"));
    }

    @Test
    void shouldReturnFraudMetrics() {
        // Arrange
        when(fraudResultRepository.getFraudMetrics()).thenReturn(expectedMetrics);

        // Act
        FraudMetrics actualMetrics = getFraudMetrics.execute();

        // Assert
        assertNotNull(actualMetrics);
        assertEquals(expectedMetrics.getTotalTransactions(), actualMetrics.getTotalTransactions());
        assertEquals(expectedMetrics.getApprovedTransactions(), actualMetrics.getApprovedTransactions());
        assertEquals(expectedMetrics.getReviewTransactions(), actualMetrics.getReviewTransactions());
        assertEquals(expectedMetrics.getAverageRiskScore(), actualMetrics.getAverageRiskScore());

        // Verify
        verify(fraudResultRepository).getFraudMetrics();
    }

    @Test
    void shouldReturnZeroMetricsWhenNoData() {
        // Arrange
        FraudMetrics zeroMetrics = new FraudMetrics();
        when(fraudResultRepository.getFraudMetrics()).thenReturn(zeroMetrics);

        // Act
        FraudMetrics actualMetrics = getFraudMetrics.execute();

        // Assert
        assertNotNull(actualMetrics);
        assertEquals(0L, actualMetrics.getTotalTransactions());
        assertEquals(0L, actualMetrics.getApprovedTransactions());
        assertEquals(0L, actualMetrics.getReviewTransactions());
        assertEquals(BigDecimal.ZERO, actualMetrics.getAverageRiskScore());

        // Verify
        verify(fraudResultRepository).getFraudMetrics();
    }
}