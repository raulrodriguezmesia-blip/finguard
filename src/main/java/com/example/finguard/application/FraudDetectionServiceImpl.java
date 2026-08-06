package com.example.finguard.application;

import com.example.finguard.domain.*;
import com.example.finguard.exception.InvalidTransactionException;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de detección de fraude.
 * Orquesta el uso de los puertos para ejecutar la lógica de negocio.
 * Incluye scoring híbrido (reglas + ML), umbrales dinámicos y métricas.
 */
public class FraudDetectionServiceImpl implements FraudDetectionService {

    private static final BigDecimal MAX_AMOUNT = new BigDecimal("100000");
    private static final BigDecimal MIN_AMOUNT = new BigDecimal("0.01");

    private final FeatureStorePort featureStorePort;
    private final PredictionPort predictionPort;
    private final StoragePort storagePort;
    private final AlertPort alertPort;
    private final MeterRegistry meterRegistry;

    // Métricas
    private final Timer evaluationTimer;
    private final Timer predictionTimer;
    private final Timer storageTimer;
    private final Timer alertTimer;
    private final AtomicInteger fraudDetectedCounter;
    private final AtomicInteger totalEvaluationsCounter;

    // Umbrales dinámicos por merchant (simulados)
    private final Map<String, Double> merchantThresholds = new ConcurrentHashMap<>();
    private final Map<String, Integer> customerTransactionCounts = new ConcurrentHashMap<>();

    public FraudDetectionServiceImpl(FeatureStorePort featureStorePort,
                                    PredictionPort predictionPort,
                                    StoragePort storagePort,
                                    AlertPort alertPort,
                                    MeterRegistry meterRegistry) {
        this.featureStorePort = featureStorePort;
        this.predictionPort = predictionPort;
        this.storagePort = storagePort;
        this.alertPort = alertPort;
        this.meterRegistry = meterRegistry;

        // Inicializar métricas
        this.evaluationTimer = meterRegistry.timer("finguard.fraud.evaluation.duration");
        this.predictionTimer = meterRegistry.timer("finguard.prediction.duration");
        this.storageTimer = meterRegistry.timer("finguard.storage.duration");
        this.alertTimer = meterRegistry.timer("finguard.alert.duration");
        this.fraudDetectedCounter = meterRegistry.gauge("finguard.fraud.detected.count", new AtomicInteger(0));
        this.totalEvaluationsCounter = meterRegistry.gauge("finguard.evaluations.total.count", new AtomicInteger(0));

        // Inicializar umbrales por defecto para algunos merchants
        merchantThresholds.put("merchant_high_risk", 0.5);
        merchantThresholds.put("merchant_medium_risk", 0.7);
        merchantThresholds.put("merchant_low_risk", 0.85);
    }

    @Override
    public FraudResult evaluateFraud(Transaction transaction) {
        Timer.Sample sample = Timer.start(meterRegistry);
        totalEvaluationsCounter.incrementAndGet();

        try {
            // 1. Validaciones robustas de entrada
            validateTransaction(transaction);

            // 2. Obtener características del cliente desde el feature store
            Map<String, Object> features = buildBaseFeatures(transaction);

            Optional<Map<String, Object>> customerFeaturesOpt =
                featureStorePort.getLatestFeatures(transaction.getCustomerId());
            customerFeaturesOpt.ifPresent(features::putAll);

            // 3. Scoring híbrido: reglas + ML
            double ruleScore = calculateRuleBasedScore(transaction, features);
            final double mlScore = predictionTimer.record(() -> {
                try {
                    return predictionPort.predictFraudScore(features);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            // Combinar scores: weighted average (60% ML, 40% reglas)
            double combinedScore = (mlScore * 0.6) + (ruleScore * 0.4);
            final double finalCombinedScore = BigDecimal.valueOf(combinedScore)
                    .setScale(4, RoundingMode.HALF_UP)
                    .doubleValue();

            // Registrar drift del modelo (simulado)
            double driftFactor = 1.0 + ((System.currentTimeMillis() / (24 * 60 * 60 * 1000)) * 0.001);
            meterRegistry.gauge("finguard.model.drift", driftFactor);

            // 4. Umbral dinámico basado en merchant y comportamiento del cliente
            double dynamicThreshold = getDynamicThreshold(transaction.getMerchantId(), transaction.getCustomerId());

            boolean isFraud = combinedScore > dynamicThreshold;

            // 5. Determinar nivel de riesgo
            String riskLevel = determineRiskLevel(combinedScore);

            // 6. Crear resultado
            FraudResult result = new FraudResult(
                transaction.getTransactionId(),
                isFraud,
                BigDecimal.valueOf(combinedScore),
                riskLevel,
                LocalDateTime.now(),
                features,
                "v1.0"
            );

            // 7. Guardar resultado (medir latencia)
            storageTimer.record(() -> storagePort.saveFraudResult(result));

            // 8. Actualizar feature store con esta transacción
            updateFeatureStore(transaction);

            // 9. Enviar alerta si es fraude
            if (isFraud) {
                fraudDetectedCounter.incrementAndGet();
                meterRegistry.counter("finguard.fraud.by.merchant", "merchantId", transaction.getMerchantId()).increment();
                sendFraudAlert(transaction, finalCombinedScore, dynamicThreshold);
            }

            return result;

        } catch (Exception e) {
            // Registrar error en métricas
            meterRegistry.counter("finguard.evaluation.errors").increment();
            throw e;
        } finally {
            sample.stop(evaluationTimer);
        }
    }

    private void sendFraudAlert(Transaction transaction, double combinedScore, double dynamicThreshold) {
        alertPort.sendFraudAlert(new FraudAlert(
            transaction.getTransactionId(),
            transaction.getCustomerId(),
            combinedScore,
            String.format("Score combinado %.4f supera umbral dinámico %.4f", combinedScore, dynamicThreshold),
            System.currentTimeMillis()
        ));
    }

    @Override
    public void updateCustomerFeatures(CustomerEvent event) {
        Objects.requireNonNull(event, "El evento no puede ser nulo");
        Map<String, Object> features = new HashMap<>();
        features.put("eventType_" + event.getEventType(), true);
        features.put("lastEventTime", event.getTimestamp());
        if (event.getEventData() instanceof Map) {
            features.putAll((Map<String, Object>) event.getEventData());
        }
        featureStorePort.updateFeatures(event.getCustomerId(), features);
    }

    private void validateTransaction(Transaction transaction) {
        if (transaction == null) {
            throw new InvalidTransactionException("La transacción no puede ser nula");
        }
        if (transaction.getTransactionId() == null || transaction.getTransactionId().isBlank()) {
            throw new InvalidTransactionException("transactionId es obligatorio");
        }
        if (transaction.getCustomerId() == null || transaction.getCustomerId().isBlank()) {
            throw new InvalidTransactionException("customerId es obligatorio");
        }
        if (transaction.getAmount() == null || transaction.getAmount().compareTo(MIN_AMOUNT) < 0 || transaction.getAmount().compareTo(MAX_AMOUNT) > 0) {
            throw new InvalidTransactionException("amount debe estar entre " + MIN_AMOUNT + " y " + MAX_AMOUNT);
        }
        if (transaction.getTimestamp() == null) {
            throw new InvalidTransactionException("timestamp es obligatorio");
        }
        if (transaction.getMerchantId() == null || transaction.getMerchantId().isBlank()) {
            throw new InvalidTransactionException("merchantId es obligatorio");
        }
    }

    private Map<String, Object> buildBaseFeatures(Transaction transaction) {
        Map<String, Object> features = new HashMap<>();
        features.put("amount", transaction.getAmount().doubleValue());
        features.put("transactionHour", transaction.getTimestamp().getHour());
        features.put("merchantCategoryCode", transaction.getMerchantCategoryCode());
        features.put("merchantId", transaction.getMerchantId());
        features.put("currency", transaction.getCurrency());
        features.put("transactionCount24h", getCustomerTransactionCount24h(transaction.getCustomerId()));
        return features;
    }

    private double calculateRuleBasedScore(Transaction transaction, Map<String, Object> features) {
        double score = 0.0;

        // Regla 1: Monto elevado
        double amount = transaction.getAmount().doubleValue();
        if (amount > 5000) score += 0.3;
        else if (amount > 1000) score += 0.15;

        // Regla 2: Horario nocturno (22:00 - 05:00)
        int hour = transaction.getTimestamp().getHour();
        if (hour >= 22 || hour <= 5) {
            score += 0.15;
        }

        // Regla 3: Categorías de comercio de alto riesgo
        String mcc = transaction.getMerchantCategoryCode();
        if (Arrays.asList("7995", "7999", "5967", "7800", "5912").contains(mcc)) {
            score += 0.25;
        }

        // Regla 4: Demasiadas transacciones en 24h
        int count24h = getCustomerTransactionCount24h(transaction.getCustomerId());
        if (count24h > 20) score += 0.3;
        else if (count24h > 10) score += 0.15;

        // Regla 5: Consistencia de moneda (si el cliente siempre usa EUR y ahora usa USD)
        if (features.containsKey("preferredCurrency") && !transaction.getCurrency().equals(features.get("preferredCurrency"))) {
            score += 0.1;
        }

        return Math.min(1.0, Math.max(0.0, score));
    }

    private double getDynamicThreshold(String merchantId, String customerId) {
        // Umbral base
        double baseThreshold = 0.7;

        // Ajustar por merchant si existe
        if (merchantThresholds.containsKey(merchantId)) {
            baseThreshold = merchantThresholds.get(merchantId);
        }

        // Ajustar por comportamiento del cliente
        int count24h = getCustomerTransactionCount24h(customerId);
        if (count24h > 20) {
            baseThreshold -= 0.1; // Más estricto si hay muchas transacciones
        }

        return Math.max(0.5, Math.min(0.95, baseThreshold));
    }

    private String determineRiskLevel(double score) {
        if (score >= 0.9) return "CRITICAL";
        if (score >= 0.7) return "HIGH";
        if (score >= 0.4) return "MEDIUM";
        return "LOW";
    }

    private int getCustomerTransactionCount24h(String customerId) {
        long now = System.currentTimeMillis();
        long from = now - (24 * 60 * 60 * 1000);
        return customerTransactionCounts.getOrDefault(customerId + "_" + (now / (24 * 60 * 60 * 1000)), 0);
    }

    private void updateFeatureStore(Transaction transaction) {
        Map<String, Object> features = new HashMap<>();
        features.put("lastAmount", transaction.getAmount().doubleValue());
        features.put("lastTimestamp", transaction.getTimestamp().toEpochSecond(ZoneOffset.UTC));
        features.put("preferredCurrency", transaction.getCurrency());
        features.put("lastMerchantId", transaction.getMerchantId());

        // Incrementar contador de transacciones del día
        long day = System.currentTimeMillis() / (24 * 60 * 60 * 1000);
        customerTransactionCounts.merge(transaction.getCustomerId() + "_" + day, 1, Integer::sum);

        featureStorePort.updateFeatures(transaction.getCustomerId(), features);
    }
}
