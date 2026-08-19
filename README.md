# AI-Powered Fintech & Cloud Observability Platform

## Overview

This platform provides financial transaction processing with real-time fraud detection capabilities. Built with a hexagonal architecture following Domain-Driven Design (DDD) principles, it's designed to be cloud-ready and observable.

## Architecture Overview

The application follows a hexagonal architecture (ports and adapters) with clear separation of concerns:

1. **Domain Layer**: Contains business logic, entities, value objects, and domain services
   - Entities: `Transaction`, `FraudResult`
   - Value Objects: `Money`, `CustomerId`
   - Domain Services: `FraudDetectionService` (with extensible rules engine)

2. **Application Layer**: Contains use cases (application services) and DTOs
   - Use Cases: `RegisterTransaction`, `GetTransactionById`, `ListCustomerTransactions`, `GetFraudMetrics`
   - DTOs for request/response validation and transformation

3. **Infrastructure Layer**: Contains technical details and adapters
   - REST Controllers exposing HTTP endpoints
   - JPA Repositories for persistence
   - Configuration classes (OpenTelemetry, exception handling)
   - External adapters for databases, messaging, etc.

## Technology Stack

- **Language**: Java 21
- **Framework**: Spring Boot 3.2.x
- **Build Tool**: Maven
- **Persistence**: JPA/Hibernate with PostgreSQL
- **API Documentation**: OpenAPI 3.0 (SpringDoc) with Swagger UI
- **Observability**: OpenTelemetry with OTLP export + Spring Boot Actuator
- **Validation**: Jakarta Validation
- **Dependency Injection**: Spring Framework
- **Testing**: JUnit 5, Mockito, Testcontainers
- **Containerization**: Docker (multi-stage build)
- **Lombok**: For reducing boilerplate code

## API Endpoints

### Transaction Management

#### Register a new transaction
```
POST /api/transactions
Content-Type: application/json

{
  "customerId": "123e4567-e89b-12d3-a456-426614174000",
  "amount": 1500.00,
  "currency": "USD",
  "timestamp": "2026-08-18T20:50:23",
  "merchantCode": "5411"
}

Response:
{
  "transactionId": "123e4567-e89b-12d3-a456-426614174001",
  "riskScore": 0.8,
  "status": "REVIEW"
}
```

#### Get transaction by ID
```
GET /api/transactions/{id}

Response:
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "customerId": "123e4567-e89b-12d3-a456-426614174000",
  "amount": 1500.00,
  "currency": "USD",
  "timestamp": "2026-08-18T20:50:23",
  "merchantCode": "5411"
}
```

#### List customer transactions
```
GET /api/customers/{customerId}/transactions

Response:
[
  {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "customerId": "123e4567-e89b-12d3-a456-426614174000",
    "amount": 1500.00,
    "currency": "USD",
    "timestamp": "2026-08-18T20:50:23",
    "merchantCode": "5411"
  }
]
```

### Fraud Metrics

#### Get fraud metrics
```
GET /api/metrics/fraud

Response:
{
  "totalTransactions": 100,
  "approvedTransactions": 80,
  "reviewTransactions": 20,
  "averageRiskScore": 0.25
}
```

## Observability

### Actuator Endpoints
- `GET /actuator/health` - Application health status
- `GET /actuator/metrics` - Application metrics (requires Prometheus or similar)
- `GET /actuator/info` - Application information

### OpenTelemetry
Traces are exported via OTLP to the endpoint specified in the `OTEL_EXPORTER_OTLP_ENDPOINT` environment variable.
By default, traces are sent to `localhost:4317` (common for Jaeger, Zipkin, or Azure Monitor).

## Deployment Instructions

### Local Deployment

#### Prerequisites
- Java JDK 21
- Maven 3.8+
- Docker (optional, for containerized deployment)
- PostgreSQL (for persistence)

#### Steps
1. Clone the repository
2. Configure database connection in `application.properties` or via environment variables
3. Run the application:
   ```bash
   mvn spring-boot:run
   ```
   Or run the JAR directly:
   ```bash
   java -jar target/observability-platform-1.0.0-SNAPSHOT.jar
   ```

#### API Documentation
Once running, access Swagger UI at:
- http://localhost:8080/swagger-ui.html
- http://localhost:8080/v3/api-docs (OpenAPI JSON)

### Containerized Deployment

#### Build the Docker image
```bash
docker build -t ai-fintech-observability-platform .
```

#### Run the container
```bash
docker run -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/observability \
  -e SPRING_DATASOURCE_USERNAME=postgres \
  -e SPRING_DATASOURCE_PASSWORD=postgres \
  -e OTEL_EXPORTER_OTLP_ENDPOINT=http://localhost:4317 \
  ai-fintech-observability-platform
```

### Cloud Deployment (Azure/AWS)

#### Environment Variables
Configure these environment variables for cloud deployment:

```
# Database Configuration
SPRING_DATASOURCE_URL=jdbc:postgresql://[HOST]:[PORT]/[DATABASE]
SPRING_DATASOURCE_USERNAME=[USERNAME]
SPRING_DATASOURCE_PASSWORD=[PASSWORD]

# OpenTelemetry Configuration
OTEL_EXPORTER_OTLP_ENDPOINT=[OTLP_ENDPOINT]  # e.g., for Azure Monitor: https://[REGION].monitor.azure.com

# Application Configuration
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=prod

# Optional: Azure Key Vault / AWS Secrets Manager integration
# (Implementation would require additional dependencies and configuration)
```

#### Azure Deployment Options
1. **Azure App Service**: Deploy the Docker container to App Service
2. **Azure Container Apps**: Deploy to serverless container platform
3. **Azure Kubernetes Service (AKS)**: Deploy to managed Kubernetes

#### AWS Deployment Options
1. **AWS ECS/EKS**: Deploy container to managed container service
2. **AWS Elastic Beanstalk**: Deploy to PaaS offering
3. **AWS Lambda (container image)**: Deploy as container function

## Health Check Endpoints

The application provides several health check endpoints for monitoring:

- `GET /actuator/health` - Overall application health
- `GET /actuator/health/liveness` - Liveness probe (Kubernetes)
- `GET /actuator/health/readiness` - Readiness probe (Kubernetes)
- `GET /api/metrics/health` - Custom application health check (returns simple JSON)

## Fraud Detection Logic

The platform implements a placeholder fraud detection service with the following rules:

1. **High Amount Rule**: If transaction amount > 1000 USD → riskScore = 0.8, status = REVIEW
2. **Business Hours Rule**: If transaction occurs outside business hours (9:00-18:00 local time) → riskScore = 0.8, status = REVIEW
3. **Default**: Otherwise → riskScore = 0.2, status = APPROVED

The `FraudDetectionService` interface is designed to allow easy injection of ML models in the future by replacing the implementation.

## Testing

### Unit Tests
Run unit tests with:
```bash
mvn test
```

### Integration Tests
Integration tests use Testcontainers to spin up a real PostgreSQL instance:
```bash
mvn verify
```

## Configuration

### application.properties
```properties
# Server
server.port=${PORT:8080}

# Database
spring.datasource.url=${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5432/observability}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME:postgres}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD:postgres}
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false

# OpenTelemetry
management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=always

# Application
spring.profiles.active=${SPRING_PROFILES_ACTIVE:dev}
```

## License

This project is licensed under the MIT License.