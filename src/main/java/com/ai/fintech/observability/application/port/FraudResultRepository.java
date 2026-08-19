package com.ai.fintech.observability.application.port;

import com.ai.fintech.observability.domain.model.FraudResult;

/**
 * Puerto (interface) para el repositorio de resultados de fraude.
 * Define el contrato para operaciones de persistencia de resultados de fraude.
 */
public interface FraudResultRepository {
    /**
     * Guarda un resultado de fraude en el repositorio.
     *
     * @param fraudResult el resultado de fraude a guardar
     * @return el resultado de fraude guardado
     */
    FraudResult save(FraudResult fraudResult);
    
    /**
     * Busca un resultado de fraude por el ID de la transacción.
     *
     * @param transactionId el ID de la transacción asociada al resultado
     * @return el resultado de fraude encontrado, o null si no existe
     */
    FraudResult findByTransactionId(String transactionId);
    
    /**
     * Obtiene métricas de fraude para reportes.
     *
     * @return objeto con métricas de fraude
     */
    FraudMetrics getFraudMetrics();
}