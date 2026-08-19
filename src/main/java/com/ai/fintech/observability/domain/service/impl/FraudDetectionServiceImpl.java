package com.ai.fintech.observability.domain.service.impl;

import com.ai.fintech.observability.domain.model.FraudResult;
import com.ai.fintech.observability.domain.model.Money;
import com.ai.fintech.observability.domain.model.Transaction;
import com.ai.fintech.observability.domain.service.FraudDetectionService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Implementación del servicio de detección de fraude con lógica placeholder.
 * Implementa reglas básicas que pueden ser extendidas o reemplazadas por ML.
 */
public class FraudDetectionServiceImpl implements FraudDetectionService {

    // Umbral de cantidad para considerar transacción como de alto riesgo
    private static final Money HIGH_AMOUNT_THRESHOLD = Money.of(1000.0, "USD");
    
    // Horario de negocio considerado seguro (9:00 - 18:00)
    private static final LocalTime BUSINESS_START = LocalTime.of(9, 0);
    private static final LocalTime BUSINESS_END = LocalTime.of(18, 0);

    @Override
    public FraudResult evaluateTransaction(Transaction transaction) {
        BigDecimal riskScore;
        String status;

        // Regla 1: Transacciones superiores a 1000 USD
        boolean isHighAmount = transaction.amount().greaterThan(HIGH_AMOUNT_THRESHOLD);
        
        // Regla 2: Transacciones fuera del horario de negocio
        LocalTime transactionTime = transaction.timestamp().toLocalTime();
        boolean isOutsideBusinessHours = 
            transactionTime.isBefore(BUSINESS_START) || 
            transactionTime.isAfter(BUSINESS_END);

        if (isHighAmount || isOutsideBusinessHours) {
            riskScore = new BigDecimal("0.8");
            status = "REVIEW";
        } else {
            riskScore = new BigDecimal("0.2");
            status = "APPROVED";
        }

        return new FraudResult(transaction.id(), riskScore, status);
    }
}