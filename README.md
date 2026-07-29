# FinGuard: AI-Powered Fintech & Cloud Observability Platform

Un sistema de detección de fraude en tiempo real con arquitectura hexagonal, backend en Java/Spring Boot, desplegado en AWS con Terraform, CI/CD con GitHub Actions, observabilidad con Micrometer/CloudWatch/Grafana y ML operativo con SageMaker.

## Características

- **Arquitectura hexagonal**: puertos y adaptadores desacoplados.
- **Backend robusto**: reglas de negocio realistas, scoring híbrido (60% ML + 40% reglas), umbrales dinámicos, validaciones robustas.
- **Concurrencia**: Virtual Threads (Java 21) para alta capacidad de procesamiento.
- **Infraestructura reproducible**: Terraform con módulos para VPC, ECS Fargate, Aurora, DynamoDB, SageMaker, SNS, IAM.
- **CI/CD profesional**: GitHub Actions con build, test, Docker, ECR, Terraform y pruebas de integración.
- **Observabilidad integrada**: métricas con Micrometer exportadas a CloudWatch, dashboard Grafana.
- **MLOps**: pipeline de entrenamiento → validación → despliegue → monitoreo, con simulación de drift.
- **Rama experimental**: Java 25 + Spring Boot 3.3.x para evaluación de última generación.

## Estructura del proyecto

```
src/main/java           # Código fuente (dominio, aplicación, adaptadores, configuración)
src/main/resources      # Configuración (application.properties)
Dockerfile              # Imagen Docker multi-stage
docker-compose.yml      # Entorno local (PostgreSQL, Kafka, app)
terraform/              # Infraestructura como código (AWS)
.github/workflows/      # Pipelines CI/CD y ML
docs/                   # Documentación, dashboards, diagramas
scripts/                # Scripts auxiliares (generación de modelo SageMaker)
```

## Prerrequisitos

- Java 21 (rama estable) o Java 25 (rama experimental)
- Maven 3.9+
- Docker
- AWS CLI (para despliegue)
- Terraform >= 1.5
- PostgreSQL (desarrollo local)
- Kafka (desarrollo local, opcional)

## Ejecución local

```bash
# Compilar y ejecutar tests
mvn clean verify

# Ejecutar la aplicación
java -jar target/finguard-0.0.1-SNAPSHOT.jar

# La API estará disponible en http://localhost:8080
```

### Docker

```bash
docker build -t finguard .
docker run -p 8080:8080 finguard
```

### Docker Compose (entorno completo local)

```bash
docker-compose up --build
```

Incluye PostgreSQL, Kafka y la aplicación FinGuard.

## API

### Evaluar transacción

```http
POST /api/v1/transactions/evaluate
Content-Type: application/json

{
  "transactionId": "tx-123",
  "customerId": "cust-456",
  "amount": 1500.00,
  "merchantId": "merchant_high_risk",
  "merchantCategoryCode": "7995",
  "timestamp": "2026-07-28T14:30:00",
  "currency": "USD",
  "paymentMethod": "card"
}
```

### Procesar evento de cliente

```http
POST /api/v1/transactions/customers/events
Content-Type: application/json

{
  "customerId": "cust-456",
  "eventType": "login",
  "eventData": { "device": "mobile" }
}
```

## Infraestructura

Ver [`terraform/README.md`](terraform/README.md) para el despliegue en AWS.

## Observabilidad

- **Métricas**: `/actuator/prometheus` (Prometheus) y exportación a CloudWatch.
- **Dashboard Grafana**: importar `docs/grafana-dashboard.json`.
- **Paneles**:
  - Latency breakdown (p50, p95, p99)
  - Fraud rate por merchant
  - Drift del modelo
  - Evaluaciones totales vs fraudes detectados
  - Errores de evaluación

## Machine Learning

Ver [`docs/ml.md`](docs/ml.md) para detalles del pipeline.

## CI/CD

- **Pipeline principal**: `.github/workflows/ci.yml`
  - Build y test con Maven.
  - Build y push de imagen Docker a ECR.
  - Terraform init/plan/apply.
  - Pruebas de integración post-despliegue.
- **Pipeline de reentrenamiento**: `.github/workflows/ml-retrain.yml`
  - Ejecución semanal o manual.
  - Entrena, valida y despliega nuevo modelo en SageMaker.
  - Notificaciones por SNS.

> **Nota**: Para despliegue a producción, configurar el environment `prod` en GitHub con aprobación manual requerida.

## Branch experimental

Ver [`docs/java25-experimental.md`](docs/java25-experimental.md) para instrucciones sobre la rama `java-25-spring-3.3`.

## Licencia

MIT
