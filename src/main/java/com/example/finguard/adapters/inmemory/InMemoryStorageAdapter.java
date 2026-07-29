package com.example.finguard.adapters.inmemory;

import com.example.finguard.domain.StoragePort;
import com.example.finguard.domain.Transaction;
import com.example.finguard.domain.FraudResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Adaptador en memoria para el puerto de almacenamiento.
 * Útil para desarrollo y pruebas.
 */
public class InMemoryStorageAdapter implements StoragePort {

    private final Map<String, Transaction> transactions = new ConcurrentHashMap<>();
    private final Map<String, FraudResult> fraudResults = new ConcurrentHashMap<>();

    @Override
    public void saveTransaction(Transaction transaction) {
        transactions.put(transaction.getTransactionId(), transaction);
    }

    @Override
    public void saveFraudResult(FraudResult result) {
        fraudResults.put(result.getTransactionId(), result);
    }

    @Override
    public List<Transaction> getCustomerTransactionHistory(String customerId, long from, long to) {
        List<Transaction> result = new ArrayList<>();
        for (Transaction tx : transactions.values()) {
            if (tx.getCustomerId().equals(customerId) &&
                tx.getTimestamp().toEpochSecond(java.time.ZoneOffset.UTC) >= from &&
                tx.getTimestamp().toEpochSecond(java.time.ZoneOffset.UTC) <= to) {
                result.add(tx);
            }
        }
        return result;
    }
}
