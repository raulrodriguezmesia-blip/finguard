package com.ai.fintech.observability.domain.service;

import com.ai.fintech.observability.domain.model.FraudResult;
import com.ai.fintech.observability.domain.model.Money;
import com.ai.fintech.observability.domain.model.Transaction;
import com.ai.fintech.observability.domain.model.CustomerId;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Servicio de dominio para detección de fraude con motor de reglas extensible.
 * Implementa lógica placeholder que puede ser reemplazada por modelos de ML posteriormente.
 */
public interface FraudDetectionService {
    /**
     * Evalúa una transacción para determinar si es fraudulenta.
     *
     * @param transaction la transacción a evaluar
     * @return el resultado de la evaluación de fraude
     */
    FraudResult evaluateTransaction(Transaction transaction);
}