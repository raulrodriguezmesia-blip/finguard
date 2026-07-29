package com.example.finguard.application;

import com.example.finguard.adapters.inmemory.InMemoryFeatureStoreAdapter;
import com.example.finguard.adapters.inmemory.InMemoryPredictionAdapter;
import com.example.finguard.adapters.inmemory.InMemoryStorageAdapter;
import com.example.finguard.adapters.inmemory.InMemoryAlertAdapter;
import com.example.finguard.domain.*;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class FraudDetectionServiceTest {

    private FraudDetectionServiceImpl service;
    private MeterRegistry meterRegistry;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        FeatureStorePort featureStore = new InMemoryFeatureStoreAdapter();
        PredictionPort prediction = new InMemoryPredictionAdapter();
        StoragePort storage = new InMemoryStorageAdapter();
        AlertPort alert = new InMemoryAlertAdapter();

        service = new FraudDetectionServiceImpl(featureStore, prediction, storage, alert, meterRegistry);
    }

    @Test
    void shouldDetectFraudForHighRiskTransaction() {
        Transaction tx = new Transaction(
                "tx-fraud-1",
                "cust-1",
                "merch_high_risk",
                new BigDecimal("6000"),
                "USD",
                LocalDateTime.now(),
                "7995",
                "card"
        );

        FraudResult result = service.evaluateFraud(tx);

        assertThat(result).isNotNull();
        assertThat(result.isFraud()).isTrue();
        assertThat(result.getFraudScore()).isGreaterThan(BigDecimal.ZERO);
        assertThat(result.getRiskLevel()).isIn("HIGH", "CRITICAL");
    }

    @Test
    void shouldNotDetectFraudForLowRiskTransaction() {
        Transaction tx = new Transaction(
                "tx-ok-1",
                "cust-2",
                "merch_low_risk",
                new BigDecimal("25"),
                "USD",
                LocalDateTime.now().withHour(14),
                "5411",
                "card"
        );

        FraudResult result = service.evaluateFraud(tx);

        assertThat(result).isNotNull();
        assertThat(result.isFraud()).isFalse();
    }

    @Test
    void shouldUpdateCustomerFeatures() {
        CustomerEvent event = new CustomerEvent("cust-1", "login", Map.of("device", "mobile"), System.currentTimeMillis());

        // No debe lanzar excepción
        service.updateCustomerFeatures(event);
    }
}
