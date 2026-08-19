# Spec: Cloud Infrastructure for AI-Powered Fintech & Cloud Observability Platform

## Objective
Construir una infraestructura cloud escalable y segura para una plataforma de fintech con capacidades de observabilidad impulsada por IA. La plataforma necesitará:
- Almacenamiento de features para modelos de ML (DynamoDB/S3)
- Capacidades de predicción en tiempo real (SageMaker)
- Procesamiento de eventos en streaming (Kafka)
- Pipeline de CI/CD robusto (GitHub Actions)
- Seguridad empresarial (JWT/OAuth2, rate limiting, RBAC)
- Observabilidad avanzada (CloudWatch, trazabilidad, dashboards)
- Optimización de performance (caché, pooling, índices)
- Deploy consistente (Terraform + Azure)
- Documentación completa (OpenAPI, diagramas, runbooks)

## Tech Stack
- AWS: DynamoDB, S3, SageMaker, CloudWatch
- Streaming: Apache Kafka (self-managed o MSK)
- CI/CD: GitHub Actions con Maven
- Seguridad: JWT, OAuth2, OPA para RBAC, rate limiting
- Observabilidad: CloudWatch, OpenTelemetry para trazabilidad, Grafana para dashboards
- Performance: Redis para caché, HikariCP para connection pooling, índices específicos en DynamoDB
- Deployment: Terraform con proveedores AWS y Azure
- Orquestación: AKS (Azure Kubernetes Service) o Azure Container Apps
- Documentación: OpenAPI 3.0, Mermaid para diagramas, plantillas de runbooks

## Commands
- Infra provision: `terraform init && terraform plan && terraform apply`
- Build: `mvn clean package`
- Test: `mvn test -DskipITs=false` (incluye pruebas de integración)
- Coverage: `mvn jacoco:report`
- Dev: `mvn spring-boot:run` (para servicios backend)
- Kafka: `kafka-topics.sh --create --topic fintech-events --bootstrap-server localhost:9092`
- Deploy to Azure: `az aks get-credentials && kubectl apply -f k8s/`

## Project Structure
```
infra/                    → Terraform modules
  ├── aws/                → Recursos AWS (DynamoDB, S3, SageMaker)
  ├── azure/              → Recursos Azure (AKS/Container Apps, VNet)
  ├── kafka/              → Configuración de Kafka/MSK
  └── monitoring/         → Configuración de CloudWatch y dashboards

src/                      → Código fuente de la aplicación
  ├── api/                → Capa de API REST/OpenAPI
  ├── feature-store/      → Lógica para DynamoDB/S3 feature store
  ├── ml-predictions/     → Integración con SageMaker para predicciones
  ├── event-streaming/    → Producers/consumers de Kafka
  ├── security/           → JWT/OAuth2, rate limiting, RBAC
  └── observability/      → Instrumentación para trazabilidad y métricas

tests/                    → Pruebas unitarias y de integración
  ├── unit/               → Pruebas unitarias
  └── integration/        → Pruebas de integración con servicios reales/mockeados

docs/                     → Documentación técnica
  ├── api/                → Especificaciones OpenAPI
  ├── architecture/       → Diagramas de arquitectura (Mermaid)
  └── runbooks/           → Procedimientos operativos

k8s/                      → Manifiestos de Kubernetes
  ├── base/               → Recursos comunes
  ├── overlays/           → Configuraciones por entorno (dev, staging, prod)
  └── monitoring/         → Prometheus, Grafana, etc.

ci/                       → Configuración de CI/CD
  └── github/             → Workflows de GitHub Actions
```

## Code Style
- Java 17+ con Spring Boot 3.x para microservicios
- Convenciones de nombrado: camelCase para variables/métodos, PascalCase para clases
- Cada microservicio en su propio paquete bajo `src/main/java/com/fintech/observability/`
- Los DTOs van en paquetes `*dto*`, entidades en `*entity*`, repositorios en `*repository*`
- Los archivos de configuración usan YAML (application-{profile}.yaml)
- Comentarios Javadoc para APIs públicas, comentarios inline para lógica compleja
- Longitud máxima de línea: 120 caracteres
- Importaciones ordenadas alfabéticamente dentro de bloques (java, javax, spring, proyecto)

## Testing Strategy
- Framework: JUnit 5 + Mockito para unitarias, Testcontainers para integración
- Ubicación de pruebas: `src/test/java` siguiendo la misma estructura de paquetes que el código fuente
- Cobertura objetivo: 85% general, 90% para código crítico de seguridad y financiero
- Niveles de prueba:
  - Unitarias: Lógica de negocio, validaciones, transformaciones de datos
  - Integración: Interacciones con DynamoDB (local con LocalStack), S3 (LocalMock), Kafka (embebido)
  - Contrato: Pruebas de contrato para APIs usando Pact o similares
  - Performance: Tests de carga con JMeter o Gatling para validar latencia y throughput
  - Seguridad: Pruebas de penetración ligeras y validación de configuraciones de seguridad

## Boundaries
- **Always:** Ejecutar pruebas locales antes de commit, validar entradas de API, seguir convenciones de nombrado
- **Ask first:** Cambios en esquemas de DynamoDB, actualización de versiones de dependencias mayores, modificaciones en políticas de IAM, cambios en topology de Kafka
- **Never:** Commit de credenciales o secrets, deshabilitar mecanismos de seguridad en producción, deploy directo a prod sin pasar por staging

## Success Criteria
- Infraestructura provisionada en menos de 20 minutos con un solo comando Terraform
- Latencia de predicción real < 100ms p95 mediante SageMaker
- Throughput de Kafka sustentado > 10k eventos/segundo con latencia < 10ms p99
- Pipeline CI/CD completo (build → test → security scan → deploy) en < 15 minutos
- Cobertura de pruebas >= 85% con zero vulnerabilidades críticas de seguridad
- Tiempo de recuperación ante falla < 5 minutos con failover automático
- Costo mensual estimado < $5000 para entorno de producción moderado
- Documentación actualizada automáticamente como parte del CI

## Open Questions
1. ¿Prefieren usar AWS MSK (Kafka gestionado) o desplegar Kafka autogestionado en EC2/EKS para mayor control?
2. ¿Debemos usar exclusivamente Azure para orquestación (AKS/Container Apps) o mantener algunos servicios en AWS (Lambda, Fargate) para aprovechar servicios específicos?
3. ¿Qué nivel de granularidad desean para los dashboards de observabilidad (métricas por negocio vs por infraestructura)?
4. ¿Existen requisitos específicos de compliance financiero (PCI-DSS, SOC2) que debamos considerar en la arquitectura de seguridad?
5. ¿Prefieren solución de caché específica (Redis, Memcached, DynamoDB DAX) o son flexibles?