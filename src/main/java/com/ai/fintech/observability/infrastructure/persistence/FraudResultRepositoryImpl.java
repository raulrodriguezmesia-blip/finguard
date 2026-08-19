package com.ai.fintech.observability.infrastructure.persistence;

import com.ai.fintech.observability.application.port.FraudResultRepository;
import com.ai.fintech.observability.domain.model.FraudResult;
import com.ai.fintech.observability.application.port.FraudMetrics;
import com.ai.fintech.observability.infrastructure.persistence.FraudResultEntity;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación JPA del repositorio de resultados de fraude.
 */
@Repository
@Transactional
public class FraudResultRepositoryImpl implements FraudResultRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public FraudResult save(FraudResult fraudResult) {
        FraudResultEntity entity = new FraudResultEntity(fraudResult);
        entityManager.merge(entity);
        return entity.toDomainModel();
    }

    @Override
    public FraudResult findByTransactionId(String transactionId) {
        FraudResultEntity entity = entityManager.find(FraudResultEntity.class, transactionId);
        return entity != null ? entity.toDomainModel() : null;
    }

    @Override
    public FraudMetrics getFraudMetrics() {
        FraudMetrics metrics = new FraudMetrics();
        
        // Contar transacciones totales
        String totalQuery = "SELECT COUNT(f) FROM FraudResultEntity f";
        Long total = entityManager.createQuery(totalQuery, Long.class).getSingleResult();
        metrics.setTotalTransactions(total != null ? total : 0);
        
        // Contar transacciones aprobadas
        String approvedQuery = "SELECT COUNT(f) FROM FraudResultEntity f WHERE f.status = 'APPROVED'";
        Long approved = entityManager.createQuery(approvedQuery, Long.class).getSingleResult();
        metrics.setApprovedTransactions(approved != null ? approved : 0);
        
        // Contar transacciones en revisión
        String reviewQuery = "SELECT COUNT(f) FROM FraudResultEntity f WHERE f.status = 'REVIEW'";
        Long review = entityManager.createQuery(reviewQuery, Long.class).getSingleResult();
        metrics.setReviewTransactions(review != null ? review : 0);
        
        // Calcular promedio de riesgo
        String avgQuery = "SELECT AVG(f.riskScore) FROM FraudResultEntity f";
        BigDecimal avgRisk = entityManager.createQuery(avgQuery, BigDecimal.class).getSingleResult();
        metrics.setAverageRiskScore(avgRisk != null ? avgRisk : BigDecimal.ZERO);
        
        return metrics;
    }
}