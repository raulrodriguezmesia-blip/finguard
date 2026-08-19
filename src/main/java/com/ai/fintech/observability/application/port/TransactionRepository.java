package com.ai.fintech.observability.application.port;

import com.ai.fintech.observability.domain.model.Transaction;
import com.ai.fintech.observability.domain.model.FraudResult;

/**
 * Puerto (interface) para el repositorio de transacciones.
 * Define el contrato para operaciones de persistencia de transacciones.
 */
public interface TransactionRepository {
    /**
     * Guarda una transacción en el repositorio.
     *
     * @param transaction la transacción a guardar
     * @return la transacción guardada
     */
    Transaction save(Transaction transaction);
    
    /**
     * Busca una transacción por su ID.
     *
     * @param id el ID de la transacción a buscar
     * @return la transacción encontrada, o null si no existe
     */
    Transaction findById(String id);
    
    /**
     * Obtiene todas las transacciones de un cliente.
     *
     * @param customerId el ID del cliente cuyas transacciones se buscarán
     * @return lista de transacciones del cliente
     */
    java.util.List<Transaction> findByCustomerId(String customerId);
}