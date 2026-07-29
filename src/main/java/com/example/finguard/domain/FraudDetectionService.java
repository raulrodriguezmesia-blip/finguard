package com.example.finguard.domain;

import java.util.Map;

/**
 * Servicio de dominio para detección de fraude.
 * Contiene la lógica de negocio pura, independiente de frameworks o infraestructura.
 */
public interface FraudDetectionService {

    /**
     * Evalúa una transacción para detectar potencial fraude.
     *
     * @param transaction La transacción a evaluar
     * @return Resultado de la evaluación de fraude
     */
    FraudResult evaluateFraud(Transaction transaction);

    /**
     * Actualiza las características de comportamiento de un cliente basado en un evento.
     * Esto se usa para mantener un feature store en tiempo real.
     *
     * @param event El evento que contiene información para actualizar características
     */
    void updateCustomerFeatures(CustomerEvent event);
}
