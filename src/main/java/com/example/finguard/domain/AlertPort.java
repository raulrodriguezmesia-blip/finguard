package com.example.finguard.domain;

/**
 * Puerto de salida para enviar alertas cuando se detecta fraude.
 * Implementado típicamente usando SNS, correo electrónico, Slack, etc.
 */
public interface AlertPort {

    /**
     * Envía una alerta de fraude.
     *
     * @param alert La alerta de fraude a enviar
     */
    void sendFraudAlert(FraudAlert alert);
}
