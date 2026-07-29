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
src/main/resources      # Configuración (application.properties, application-prod.properties)
src/test                # Tests unitarios e integración
Dockerfile              # Imagen Docker multi-stage
docker-compose.yml      # Entorno local (PostgreSQL, Kafka, app)
terraform/              # Infraestructura como código (AWS)
.github/workflows/      # Pipelines CI/CD y ML
docs/                   # Documentación, dashboards, diagramas
scripts/                # Scripts auxiliares (bootstrap AWS, despliegue, modelo SageMaker)
```

## Prerrequisitos

- Java 21 (rama estable) o Java 25 (rama experimental)
- Maven 3.9+
- Docker
- AWS CLI configurado con credenciales
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

### Bootstrap del estado remoto de Terraform

Antes de aplicar módulos, crear el bucket S3 y la tabla DynamoDB para estado remoto y lock:

```bash
# Opción 1: manual desde AWS Console
# - Crear bucket S3: finguard-terraform-state
# - Crear tabla DynamoDB: finguard-terraform-locks (PK: LockID)

# Opción 2: usando el módulo bootstrap (requiere AWS CLI configurado)
cd scripts
./setup_aws.ps1
```

### Despliegue por ambientes

```bash
# Dev
terraform init -backend-config=terraform/backend-dev.hcl
terraform plan -var-file=terraform/terraform.tfvars
terraform apply -var-file=terraform/terraform.tfvars

# Staging
terraform init -backend-config=terraform/backend-staging.hcl
terraform plan -var-file=terraform/terraform.tfvars
terraform apply -var-file=terraform/terraform.tfvars

# Prod (con aprobación manual en GitHub Actions)
terraform init -backend-config=terraform/backend-prod.hcl
terraform plan -var-file=terraform/terraform.tfvars
terraform apply -var-file=terraform/terraform.tfvars
```

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

### Secrets requeridos en GitHub Actions

Configurar en el repositorio → Settings → Secrets and variables → Actions:

| Nombre | Descripción |
|--------|-------------|
| `AWS_ACCESS_KEY_ID` | Credenciales AWS con permisos para ECR, Terraform, SageMaker, SNS, DynamoDB |
| `AWS_SECRET_ACCESS_KEY` | Credenciales AWS |
| `ECR_REPOSITORY` | Nombre del repositorio ECR (ej: `finguard`) |

Adicionalmente, crear el bucket S3 `finguard-terraform-state` y la tabla DynamoDB `finguard-terraform-locks` antes del primer deploy.

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

## Profiles de Spring Boot

| Perfil | Uso |
|--------|-----|
| `default` / local | Desarrollo local con PostgreSQL local |
| `prod` | AWS: Secrets Manager, CloudWatch, variables de entorno |
| `test` | Tests unitarios e integración con H2 en memoria |

## Seguridad

- **AWS Secrets Manager**: en perfil `prod`, `SecretsManagerConfig` carga secretos automáticamente.
- **IAM mínimo privilegio**: roles separados para ECS, SageMaker y acceso a recursos.
- **Terraform remote state**: S3 + DynamoDB para lock y cifrado.

## Branch experimental

Ver [`docs/java25-experimental.md`](docs/java25-experimental.md) para instrucciones sobre la rama `java-25-spring-3.3`.

## Comandos útiles

```bash
# Compilar
mvn clean verify

# Ejecutar local
java -jar target/finguard-0.0.1-SNAPSHOT.jar

# Docker
docker build -t finguard .
docker-compose up --build

# Terraform
terraform init -backend-config=terraform/backend-dev.hcl
terraform plan -var-file=terraform/terraform.tfvars
terraform apply -var-file=terraform/terraform.tfvars

# Pipeline ML
aws sagemaker wait endpoint-in-service --endpoint-name finguard-prod-endpoint
```

## Licencia

MIT
