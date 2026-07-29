package com.example.finguard.domain;

import java.util.Map;

/**
 * Puerto de salida para la predicción de fraude usando modelos de Machine Learning.
 * Implementado típicamente llamando a un endpoint de SageMaker u otro servicio de ML.
 */
public interface PredictionPort {

    /**
     * Predice la probabilidad de fraude basado en las características proporcionadas.
     *
     * @param features Características de la transacción y cliente
     * @return Score de fraude entre 0 y 1
     */
    Double predictFraudScore(Map<String, Object> features);
}
