package com.example.finguard.adapters.inmemory;

import com.example.finguard.domain.FeatureStorePort;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Adaptador en memoria para el puerto de feature store.
 * Almacena características de clientes en memoria con TTL simulado (1 hora).
 */
public class InMemoryFeatureStoreAdapter implements FeatureStorePort {

    private static final long TTL_MILLIS = 60 * 60 * 1000; // 1 hora
    private final Map<String, Map<String, Object>> features = new ConcurrentHashMap<>();
    private final Map<String, Long> timestamps = new ConcurrentHashMap<>();

    @Override
    public Optional<Map<String, Object>> getLatestFeatures(String customerId) {
        Long ts = timestamps.get(customerId);
        if (ts == null || (System.currentTimeMillis() - ts) > TTL_MILLIS) {
            // Expirado o no existe
            return Optional.empty();
        }
        return Optional.ofNullable(features.get(customerId))
                       .map(java.util.HashMap::new);
    }

    @Override
    public void updateFeatures(String customerId, Map<String, Object> eventData) {
        features.put(customerId, new ConcurrentHashMap<>(eventData));
        timestamps.put(customerId, System.currentTimeMillis());
    }
}
