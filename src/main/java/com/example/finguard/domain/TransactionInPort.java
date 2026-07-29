package com.example.finguard.domain;

/**
 * Puerto de entrada para recibir transacciones a procesar.
 * Puede ser implementado por adaptadores REST, Kafka, etc.
 */
public interface TransactionInPort {

    /**
     * Procesa una transacción entrante y devuelve el resultado de evaluación de fraude.
     * 
     * @param transaction La transacción a evaluar
     * @return Resultado de la evaluación de fraude
     */
    FraudResult processTransaction(Transaction transaction);
}