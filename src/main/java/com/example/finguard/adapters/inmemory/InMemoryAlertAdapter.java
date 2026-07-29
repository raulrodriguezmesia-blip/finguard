package com.example.finguard.adapters.inmemory;

import com.example.finguard.domain.AlertPort;
import com.example.finguard.domain.FraudAlert;

/**
 * Adaptador en memoria para el puerto de alertas.
 * Simplemente imprime las alertas en la consola.
 */
public class InMemoryAlertAdapter implements AlertPort {

    @Override
    public void sendFraudAlert(FraudAlert alert) {
        System.out.println("🚨 ALERTA DE FRAUDE 🚨");
        System.out.println("Transacción: " + alert.getTransactionId());
        System.out.println("Cliente: " + alert.getCustomerId());
        System.out.println("Score: " + alert.getFraudScore());
        System.out.println("Razón: " + alert.getReason());
        System.out.println("Timestamp: " + new java.util.Date(alert.getTimestamp()));
        System.out.println("-----------------------");
    }
}
