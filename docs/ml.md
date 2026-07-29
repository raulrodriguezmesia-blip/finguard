# FinGuard ML Pipeline

## Visión general

El pipeline de ML de FinGuard sigue un ciclo MLOps: entrenamiento → validación → despliegue → monitoreo.

## 1. Entrenamiento

- **Dataset**: transacciones etiquetadas (fraude / no fraude) con características de usuario, merchant, monto, hora, etc.
- **Modelo**: LogisticRegression (baseline) o XGBoost (producción).
- **Script**: `scripts/generate_sagemaker_model.py` genera un modelo Scikit-learn dummy, lo empaqueta en `model.tar.gz` y lo sube a S3.

```bash
python scripts/generate_sagemaker_model.py
```

## 2. Validación

- Métricas: AUC, precision, recall, F1.
- Validación de drift: comparar distribución de features en entrenamiento vs producción (Kolmogorov-Smirnov).
- Aprobación manual antes de pasar a producción.

## 3. Despliegue

- Terraform crea el endpoint de SageMaker (`modules/sagemaker/main.tf`).
- La aplicación consulta el endpoint mediante `PredictionPort`.
- Versionado: cada modelo se despliega con un nombre de endpoint único (v1, v2, etc.).

## 4. Monitoreo

- **Drift simulado**: `InMemoryPredictionAdapter` aumenta el score base en un 0.1% por día para simular degradación.
- **Métricas**: `finguard.model.drift` (gauge) exportado a CloudWatch.
- **Alertas**: SNS envía alertas cuando el drift supera un threshold o la tasa de fraude se dispara.

## 5. Reentrenamiento

- Trigger: drift > threshold o performance degradation.
- Pipeline automático: `.github/workflows/ml-retrain.yml` reentrena y despliega un nuevo modelo.
- Aprobación manual antes de aplicar cambios en producción.

## Artefactos

- `docs/grafana-dashboard.json`: dashboard con latency breakdown, fraud rate por merchant y drift.
- `scripts/generate_sagemaker_model.py`: generador de modelo dummy para pruebas.
