package com.example.finguard.domain;

import java.util.List;
import java.util.Map;

/**
 * Puerto de salida para persistir transacciones y resultados de evaluación.
 * Implementado típicamente con una base de datos transaccional como Aurora PostgreSQL.
 */
public interface StoragePort {

    /**
     * Guarda una transacción en el almacén de datos.
     *
     * @param transaction La transacción a guardar
     */
    void saveTransaction(Transaction transaction);

    /**
     * Guarda el resultado de evaluación de fraude para una transacción.
     *
     * @param result El resultado de fraude a guardar
     */
    void saveFraudResult(FraudResult result);

    /**
     * Obtiene el historial de transacciones de un cliente dentro de un rango de tiempo.
     *
     * @param customerId ID del cliente
     * @param from Tiempo de inicio (epoch seconds)
     * @param to Tiempo de fin (epoch seconds)
     * @return Lista de transacciones
     */
    List<Transaction> getCustomerTransactionHistory(String customerId, long from, long to);
}
