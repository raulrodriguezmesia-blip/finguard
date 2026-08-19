package com.ai.fintech.observability.infrastructure.persistence;

import com.ai.fintech.observability.application.port.TransactionRepository;
import com.ai.fintech.observability.domain.model.Transaction;
import com.ai.fintech.observability.infrastructure.persistence.TransactionEntity;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación JPA del repositorio de transacciones.
 */
@Repository
@Transactional
public class TransactionRepositoryImpl implements TransactionRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Transaction save(Transaction transaction) {
        TransactionEntity entity = new TransactionEntity(transaction);
        if (entity.getId() == null || entity.getId().isEmpty()) {
            entity.setId(UUID.randomUUID().toString());
        }
        entityManager.merge(entity);
        return entity.toDomainModel();
    }

    @Override
    public Transaction findById(String id) {
        TransactionEntity entity = entityManager.find(TransactionEntity.class, id);
        return entity != null ? entity.toDomainModel() : null;
    }

    @Override
    public java.util.List<Transaction> findByCustomerId(String customerId) {
        String jpql = "SELECT t FROM TransactionEntity t WHERE t.customerId = :customerId";
        TypedQuery<TransactionEntity> query = entityManager.createQuery(jpql, TransactionEntity.class);
        query.setParameter("customerId", customerId);
        
        return query.getResultList().stream()
                .map(TransactionEntity::toDomainModel)
                .collect(Collectors.toList());
    }
}