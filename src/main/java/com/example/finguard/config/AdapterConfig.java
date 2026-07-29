package com.example.finguard.config;

import com.example.finguard.adapters.inmemory.InMemoryFeatureStoreAdapter;
import com.example.finguard.adapters.inmemory.InMemoryPredictionAdapter;
import com.example.finguard.adapters.inmemory.InMemoryStorageAdapter;
import com.example.finguard.adapters.inmemory.InMemoryAlertAdapter;
import com.example.finguard.application.FraudDetectionServiceImpl;
import com.example.finguard.domain.*;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.*;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de Spring que conecta los puertos con sus implementaciones.
 * Para desarrollo, usa adaptadores en memoria. Para producción, se cambiarían
 * por implementaciones de AWS (SageMaker, DynamoDB, etc.).
 */
@Configuration
public class AdapterConfig {

    @Bean
    public FeatureStorePort featureStorePort() {
        return new InMemoryFeatureStoreAdapter();
    }

    @Bean
    public PredictionPort predictionPort() {
        return new InMemoryPredictionAdapter();
    }

    @Bean
    public StoragePort storagePort() {
        return new InMemoryStorageAdapter();
    }

    @Bean
    public AlertPort alertPort() {
        return new InMemoryAlertAdapter();
    }

    @Bean
    public FraudDetectionService fraudDetectionService(
            FeatureStorePort featureStorePort,
            PredictionPort predictionPort,
            StoragePort storagePort,
            AlertPort alertPort,
            MeterRegistry meterRegistry) {
        return new FraudDetectionServiceImpl(
                featureStorePort,
                predictionPort,
                storagePort,
                alertPort,
                meterRegistry);
    }
}