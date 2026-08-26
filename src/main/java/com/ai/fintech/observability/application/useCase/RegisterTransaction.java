package com.ai.fintech.observability.application.useCase;

import com.ai.fintech.observability.application.port.TransactionRepository;
import com.ai.fintech.observability.application.port.FraudResultRepository;
import com.ai.fintech.observability.domain.model.Transaction;
import com.ai.fintech.observability.domain.model.FraudResult;
import com.ai.fintech.observability.domain.service.FraudDetectionService;
import com.ai.fintech.observability.domain.model.FraudResult;

/**
 * Caso de uso para registrar una nueva transacción y evaluarla para fraude.
 * Implementa el patrón Command de CQRS.
 */
@Service`npublic class RegisterTransaction {
    private final TransactionRepository transactionRepository;
    private final FraudDetectionService fraudDetectionService;
    private final FraudResultRepository fraudResultRepository;

    public RegisterTransaction(
            TransactionRepository transactionRepository,
            FraudDetectionService fraudDetectionService,
            FraudResultRepository fraudResultRepository) {
        this.transactionRepository = transactionRepository;
        this.fraudDetectionService = fraudDetectionService;
        this.fraudResultRepository = fraudResultRepository;
    }

    /**
     * Ejecuta el caso de uso para registrar una transacción.
     *
     * @param transaction la transacción a registrar
     * @return el resultado de la evaluación de fraude
     */
    public FraudResult execute(Transaction transaction) {
        // Guardar la transacción
        Transaction savedTransaction = transactionRepository.save(transaction);
        
        // Evaluar para fraude
        FraudResult fraudResult = fraudDetectionService.evaluateTransaction(savedTransaction);
        
        // Guardar el resultado de fraude
        fraudResultRepository.save(fraudResult);
        
        return fraudResult;
    }
}
