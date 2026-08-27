package com.ai.fintech.observability.application.useCase;

import com.ai.fintech.observability.application.port.TransactionRepository;
import com.ai.fintech.observability.domain.model.Transaction;
import org.springframework.stereotype.Service;

/**
 * Caso de uso para obtener una transacción por su ID.
 * Implementa el patrón Query de CQRS.
 */
@Service
public class GetTransactionById {
    private final TransactionRepository transactionRepository;

    public GetTransactionById(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    /**
     * Ejecuta el caso de uso para obtener una transacción por su ID.
     *
     * @param id el ID de la transacción a buscar
     * @return la transacción encontrada, o null si no existe
     */
    public Transaction execute(String id) {
        return transactionRepository.findById(id);
    }
}
