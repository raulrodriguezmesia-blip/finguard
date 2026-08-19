package com.ai.fintech.observability.application.useCase;

import com.ai.fintech.observability.application.port.FraudResultRepository;
import com.ai.fintech.observability.application.port.FraudMetrics;

/**
 * Caso de uso para obtener métricas de fraude.
 * Implementa el patrón Query de CQRS.
 */
public class GetFraudMetrics {
    private final FraudResultRepository fraudResultRepository;

    public GetFraudMetrics(FraudResultRepository fraudResultRepository) {
        this.fraudResultRepository = fraudResultRepository;
    }

    /**
     * Ejecuta el caso de uso para obtener métricas de fraude.
     *
     * @return las métricas de fraude
     */
    public FraudMetrics execute() {
        return fraudResultRepository.getFraudMetrics();
    }
}