package com.example.finguard.adapters.inmemory;

import com.example.finguard.domain.PredictionPort;

import java.util.Map;
import java.util.Random;

/**
 * Adaptador en memoria para el puerto de predicción.
 * Simula un modelo de ML que devuelve scores basados en reglas simples.
 * Incluye simulación de drift: el score base aumenta gradualmente con el tiempo.
 */
public class InMemoryPredictionAdapter implements PredictionPort {

    private final Random random = new Random();

    private double getDriftFactor() {
        long daysSinceEpoch = System.currentTimeMillis() / (24 * 60 * 60 * 1000);
        return 1.0 + (daysSinceEpoch * 0.001);
    }

    @Override
    public Double predictFraudScore(Map<String, Object> features) {
        double score = 0.0;
        double drift = getDriftFactor();

        // Lógica simple de ejemplo basada en el monto (afectada por drift)
        if (features.containsKey("amount")) {
            double amount = ((Number) features.get("amount")).doubleValue();
            if (amount > 1000) score += 0.4 * drift;
            if (amount > 5000) score += 0.3 * drift;
        }

        // Hora del día (más riesgo en horas nocturnas)
        if (features.containsKey("transactionHour")) {
            int hour = ((Number) features.get("transactionHour")).intValue();
            if (hour >= 22 || hour <= 5) {
                score += 0.2 * drift;
            }
        }

        // Algunos códigos de categoría de comerciante son más riesgosos
        if (features.containsKey("merchantCategoryCode")) {
            String mcc = (String) features.get("merchantCategoryCode");
            if (java.util.Arrays.asList("7995", "7999", "5967", "7800").contains(mcc)) {
                score += 0.3 * drift;
            }
        }

        // Características del cliente (si están disponibles)
        if (features.containsKey("transactionCount24h")) {
            double count = ((Number) features.get("transactionCount24h")).doubleValue();
            if (count > 10) score += 0.2;
            if (count > 20) score += 0.3;
        }

        // Asegurar que esté entre 0 y 1
        return Math.min(1.0, Math.max(0.0, score));
    }
}
