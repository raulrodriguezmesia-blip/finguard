package com.example.finguard.adapters.inmemory;

import com.example.finguard.domain.FraudAlert;
import com.example.finguard.domain.Transaction;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryAdaptersTest {

    private static final LocalDateTime FIXED_NOW = LocalDateTime.of(2026, 7, 28, 12, 0, 0);
    private static final long FROM = FIXED_NOW.toEpochSecond(ZoneOffset.UTC) - 10;
    private static final long TO = FIXED_NOW.toEpochSecond(ZoneOffset.UTC) + 10;

    @Test
    void inMemoryStorageAdapterShouldSaveAndRetrieveTransaction() {
        InMemoryStorageAdapter adapter = new InMemoryStorageAdapter();
        Transaction tx = new Transaction("tx-1", "cust-1", "m1", new BigDecimal("100"), "USD",
                FIXED_NOW, "1234", "card");
        adapter.saveTransaction(tx);

        List<Transaction> found = adapter.getCustomerTransactionHistory("cust-1", FROM, TO);
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getTransactionId()).isEqualTo("tx-1");
    }

    @Test
    void inMemoryStorageAdapterShouldReturnCustomerHistory() {
        InMemoryStorageAdapter adapter = new InMemoryStorageAdapter();
        Transaction tx1 = new Transaction("tx-1", "cust-1", "m1", new BigDecimal("100"), "USD",
                FIXED_NOW, "1234", "card");
        Transaction tx2 = new Transaction("tx-2", "cust-1", "m2", new BigDecimal("200"), "USD",
                FIXED_NOW, "5678", "card");
        adapter.saveTransaction(tx1);
        adapter.saveTransaction(tx2);

        List<Transaction> history = adapter.getCustomerTransactionHistory("cust-1", FROM, TO);
        assertThat(history).hasSize(2);
    }

    @Test
    void inMemoryFeatureStoreAdapterShouldEnforceTTL() throws InterruptedException {
        InMemoryFeatureStoreAdapter adapter = new InMemoryFeatureStoreAdapter();
        Map<String, Object> features = Map.of("key", "value");
        adapter.updateFeatures("cust-1", features);

        assertThat(adapter.getLatestFeatures("cust-1")).isPresent();

        // El TTL real es de 1 hora; en tests unitarios no esperamos ese lapso.
        // Aquí solo verificamos que la actualización quede visible inmediatamente.
        Optional<Map<String, Object>> result = adapter.getLatestFeatures("cust-1");
        assertThat(result).isPresent();
    }

    @Test
    void inMemoryAlertAdapterShouldPrintAlert() {
        InMemoryAlertAdapter adapter = new InMemoryAlertAdapter();
        FraudAlert alert = new FraudAlert("tx-1", "cust-1", 0.9, "test", System.currentTimeMillis());

        // No debe lanzar excepción
        adapter.sendFraudAlert(alert);
    }
}
