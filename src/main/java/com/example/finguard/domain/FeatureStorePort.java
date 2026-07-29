package com.example.finguard.domain;

import java.util.Map;
import java.util.Optional;

/**
 * Puerto de salida para obtener y almacenar características de clientes en tiempo real.
 * Implementado típicamente con una base de datos de baja latencia como DynamoDB o Redis.
 */
public interface FeatureStorePort {

    /**
     * Obtiene las características más recientes para un cliente.
     *
     * @param customerId ID del cliente
     * @return Mapa de características o vacío si no existen
     */
    Optional<Map<String, Object>> getLatestFeatures(String customerId);

    /**
     * Actualiza las características de un cliente con un nuevo evento.
     *
     * @param customerId ID del cliente
     * @param eventData Datos del evento para actualizar características
     */
    void updateFeatures(String customerId, Map<String, Object> eventData);
}
