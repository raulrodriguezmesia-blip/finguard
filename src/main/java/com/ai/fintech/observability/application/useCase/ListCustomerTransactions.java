package com.ai.fintech.observability.application.useCase;

import com.ai.fintech.observability.application.port.TransactionRepository;
import com.ai.fintech.observability.domain.model.Transaction;

import java.util.List;

/**
 * Caso de uso para listar todas las transacciones de un cliente.
 * Implementa el patrón Query de CQRS.
 */
public class ListCustomerTransactions {
    private final TransactionRepository transactionRepository;

    public ListCustomerTransactions(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    /**
     * Ejecuta el caso de uso para listar transacciones de un cliente.
     *
     * @param customerId el ID del cliente cuyas transacciones se listarán
     * @return lista de transacciones del cliente
     */
    public List<Transaction> execute(String customerId) {
        return transactionRepository.findByCustomerId(customerId);
    }
}