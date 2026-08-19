# Documentación de la Plataforma AI-Powered Fintech & Cloud Observability

## Visión General
Esta documentación cubre todos los aspectos de la plataforma AI-Powered Fintech & Cloud Observability, incluyendo arquitectura, decisiones de diseño, APIs, procedimientos operativos y guías de desarrollo.

## Table of Contents
1. [Arquitectura y Decisiones de Diseño](#arquitectura-y-decisiones-de-diseño)
2. [Guía de Desarrollo](#guía-de-desarrollo)
3. [Referencia de API](#referencia-de-api)
4. [Procedimientos Operativos](#procedimientos-operativos)
5. [Guía de Despliegue](#guía-de-despliegue)

## Arquitectura y Decisiones de Diseño

### Decisiones Arquitectónicas Clave (ADRs)

#### ADR-001: Uso de Redis para capa de caché primaria
**Status:** Accepted  
**Date:** 2026-08-07  
**Context:** Necesitamos reducir la latencia de acceso a datos frecuentemente utilizados como feature stores y resultados de predicciones.  
**Decision:** Usar AWS ElastiCache con Redis como capa de caché primaria debido a su rendimiento, escalabilidad y integración nativa con AWS.  
**Alternatives Considered:**
- **Memcached:** Rechazado porque Redis ofrece estructuras de datos más ricas y mejor persistencia
- **Caché local (Caffeine):** Rechazado porque no escala horizontalmente en ambientes distribuidos
- **DAX (DynamoDB Accelerator):** Considerado pero rechazado porque necesitamos caché para múltiples tipos de almacenes de datos, no solo DynamoDB  
**Consequences:**
- Mayor complejidad operativa gestionando un componente adicional
- Mejora significativa en latencia de lectura para datos cacheados
- Necesidad de implementar estrategias de invalidación de caché
- Costo adicional pero justificado por mejora en experiencia de usuario

#### ADR-002: Estrategia de despliegue híbrida (AKS + Container Apps)
**Status:** Accepted  
**Date:** 2026-08-07  
**Context:** Necesitamos desplegar diversos tipos de cargas de trabajo con diferentes requisitos de escalabilidad y gestión.  
**Decision:** Usar Azure Kubernetes Service (AKS) para cargas de trabajo que requieren orquestación compleja y Azure Container Apps para servicios sin servidor y de corta duración.  
**Alternatives Considered:**
- **AKS exclusivamente:** Rechazado porque algunos servicios se benefician más del modelo sin servidor
- **Container Apps exclusivamente:** Rechazado porque algunas cargas de trabajo necesitan más control sobre el ambiente de ejecución
- **Azure Functions:** Rechazado porque tiene límites de ejecución y menos flexibilidad que Container Apps  
**Consequences:**
- Mayor complejidad en gestión de dos plataformas de despliegue diferentes
- Optimización de costos al usar el servicio apropiado para cada tipo de carga de trabajo
- Necesidad de establecer comunicación segura entre servicios en diferentes plataformas
- Flexibilidad para escalar cada tipo de servicio independientemente

#### ADR-003: Implementación de trazabilidad distribuida con OpenTelemetry
**Status:** Accepted  
**Date:** 2026-08-07  
**Context:** Necesitamos diagnosticar problemas de rendimiento y errores que atraviesan múltiples servicios.  
**Decision:** Implementar OpenTelemetry como estándar vendor-neutral para trazabilidad distribuida con exportación a Azure Monitor.  
**Alternatives Considered:**
- **Jaeger:** Rechazado porque requiere gestión adicional de infraestructura y Azure tiene solución nativa mejor integrada
- **Zipkin:** Rechazado por similar razón a Jaeger, menos integrado con el ecosistema de Azure
- **Solución propietaria de un proveedor de APM:** Rechazado porque crea vendor lock-in y OpenTelemetry permite cambiar de backend fácilmente  
**Consequences:**
- Sobrecarga ligera de rendimiento debido al muestreo de traces
- Mayor visibilidad en el comportamiento del sistema y capacidad de diagnóstico
- Necesidad de entrenar al equipo en conceptos de trazabilidad distribuida
- Estándar abierto que facilita cambios futuros en el backend de observabilidad

### Decisiones de Tecnología

#### Almacenamiento de Datos
- **Feature Store:** AWS DynamoDB con caché Redis para lecturas frecuentes
- **Almacenamiento de Objetos:** Amazon S3 para almacenamiento de datasets de entrenamiento y modelos
- **Base de Datos Relacional:** Azure PostgreSQL para datos transaccionales y de usuarios
- **Almacenamiento Temporal:** Redis para sesiones y estados temporales

#### Procesamiento y Computación
- **Predicciones en Tiempo Real:** AWS SageMaker para hosting e inferencia de modelos de ML
- **Procesamiento de Eventos:** Apache Kafka (a través de Confluent Cloud) para streaming de eventos
- **Orquestación de Servicios:** Azure Kubernetes Service (AKS) y Azure Container Apps
- **Computación Sin Servidor:** Azure Container Apps para tareas específicas y webhooks

#### Observabilidad y Monitoreo
- **Métricas y Logging:** Amazon CloudWatch como backend unificado
- **Trazabilidad Distribuida:** OpenTelemetry con exportación a Azure Monitor
- **Dashboards:** Azure Monitor y Grafana para visualización de métricas
- **Alerting:** Alertas symptom-based configuradas en Azure Monitor

#### Comunicación y Seguridad
- **API Gateway:** Azure API Management para gestión, seguridad y rate limiting de APIs
- **Service-to-Service Communication:** mTLS para autenticación mutua entre servicios
- **Autenticación y Autorización:** Azure AD B2C para identidad de usuarios y OPA para decisiones de autorización granulares
- **Manejo de Secrets:** Azure Key Vault para almacenamiento seguro de credenciales y certificados

## Guía de Desarrollo

### Configuración del Ambiente de Desarrollo
1. Clonar el repositorio: `git clone <repository-url>`
2. Instalar dependencias: `mvn clean install -DskipTests`
3. Configurar variables de ambiente: copiar `.env.example` a `.env` y completar valores
4. Ejecutar aplicación localmente: `mvn spring-boot:run`

### Estándares de Codificación
- **Java:** Usar Java 21 con convenciones de Google Java Style
- **Nombres:** camelCase para variables y métodos, PascalCase para clases
- **Paquetes:** Organización por dominio de negocio (feature-store, ml-predictions, event-streaming, etc.)
- **Comentarios:** Javadoc para APIs públicas, comentarios inline para lógica no obvia
- **Formato:** 4 espacios para indentación, longitud máxima de línea 120 caracteres

### Flujo de Trabajo de Desarrollo
1. Crear rama desde `main`: `git checkout -b feature/nombre-feature`
2. Implementar cambios siguiendo TDD cuando sea posible
3. Ejecutar pruebas locales: `mvn test`
4. Solicitar revisión de código mediante Pull Request
5. Abordar comentarios de revisores
6. Merge a `main` después de aprobación

### Pruebas
- **Pruebas Unitarias:** JUnit 5 + Mockito
- **Pruebas de Integración:** Testcontainers para simular dependencias externas
- **Cobertura Objetivo:** 85% general, 90% para código crítico
- **Ejecución:** `mvn test` para unitarias, `mvn verify` para incluir integración

## Referencia de API

### Endpoints Principales

#### Transacciones Financieras
```
POST /api/transactions
Evaluar una transacción para detección de fraude
- Request: Transaction object
- Response: FraudResult with score and recommendation

GET /api/transactions/{id}
Obtener detalles de una transacción específica
- Response: Transaction object

GET /api/transactions
Listar transacciones con filtros opcionales
- Query Parameters: customerId, dateRange, minAmount, maxAmount
- Response: Lista paginada de Transaction objects
```

#### Gestión de Clientes
```
POST /api/customers
Crear un nuevo perfil de cliente
- Request: Customer object
- Response: Created customer with ID

GET /api/customers/{id}
Obtener información de un cliente específico
- Response: Customer object

GET /api/customers/{id}/transactions
Obtener historial de transacciones de un cliente
- Response: Lista paginada de Transaction objects
```

#### Métricas y Monitoreo
```
GET /api/metrics/health
Endpoint de health check para monitoreo
- Response: Status object with service health indicators

GET /api/metrics/prometheus
Endpoint para scrapping de métricas Prometheus
- Response: Métricas en formato texto plano
```

### Modelos de Datos Principales

#### Transaction
- `transactionId`: String (identificador único)
- `customerId`: String (referencia al cliente)
- `amount`: BigDecimal (monto de la transacción)
- `timestamp`: LocalDateTime (fecha y hora)
- `merchantId`: String (identificador del comercio)
- `merchantCategoryCode`: String (código de categoría de comercio)
- `currency`: String (código de moneda ISO 4217)
- `paymentMethod`: String (tarjeta, transferencia, etc.)

#### FraudResult
- `transactionId`: String (identificador de la transacción relacionada)
- `customerId`: String (referencia al cliente)
- `isFraud`: Boolean (resultado de la evaluación)
- `fraudScore`: BigDecimal (puntaje de probabilidad de fraude 0-1)
- `riskLevel`: String (LOW, MEDIUM, HIGH, CRITICAL)
- `evaluationTimestamp`: LocalDateTime (cuando se realizó la evaluación)
- `featuresUsed`: Map<String, Object> (características utilizadas en la evaluación)
- `modelVersion`: String (versión del modelo utilizado)

## Procedimientos Operativos

### Runbooks de Operaciones Comunes

#### RB-001: Despliegue de Nueva Versión
**Propósito:** Desplegar una nueva versión de la aplicación en ambiente de staging o producción  
**Requisitos Previos:**
- Código mergeado a rama `main`
- Pipeline CI completado exitosamente
- Acceso a entorno destino con permisos apropiados  
**Pasos:**
1. Verificar que el pipeline CI haya completado exitosamente
2. Navegar al directorio de Terraform: `cd terraform`
3. Seleccionar el workspace apropiado: `terraform workspace select <environment>`
4. Revisar el plan de cambios: `terraform plan`
5. Aplicar los cambios: `terraform apply -auto-approve`
6. Verificar health checks de todos los servicios
7. Notificar a stakeholders del despliegue completado  
**Procedimiento de Reversión:**
1. En caso de problemas críticos, ejecutar: `terraform rollback`
2. Verificar que servicios regresen al estado anterior
3. Notificar a stakeholders sobre la reversión

#### RB-002: Escalado de Recursos
**Propósito:** Ajustar capacidad de recursos basado en demanda observada  
**Requisitos Previos:**
- Acceso a consola de Azure con permisos de administrador
- Métricas de uso actuales disponibles  
**Pasos para Escalar AKS:**
1. Navegar a Azure Portal > AKS Cluster > Scale
2. Ajustar número de nodos en el node pool principal
3. Opcional: Ajustar tamaños de nodos según carga de trabajo
4. Guardar cambios y esperar propagación
5. Verificar que nuevos nodos estén listos y recibiendo tráfico  
**Pasos para Escalar Redis:**
1. Navegar a Azure Portal > Redis Cache > Scale
2. Ajustar tamaño de caché según necesidades de memoria
3. Opcional: Cambiar nivel de rendimiento (Basic, Standard, Premium)
4. Guardar cambios y esperar propagación
3. Verificar que caché continúa funcionando normalmente  
**Procedimientos de Monitoreo Post-Escalado:**
1. Verificar métricas de utilización de recursos
2. Confirmar que latencia de respuesta mejora o se estabiliza
3. Monitorear durante 24 horas para estabilización

#### RB-003: Respuesta a Incidente de Seguridad
**Propósito:** Responder efectivamente a un incidente de seguridad potencial  
**Indicadores de Posible Incidente:**
- Alertas de rate limiting excesivo en endpoints de autenticación
- Inicios de sesión fallidos en secuencia desde misma IP
- Descarga inusual de datos o exportaciones no autorizadas
- Alertas de Azure Security Center o Microsoft Defender  
**Pasos Inmediatos:**
1. Activar equipo de respuesta a incidentes de seguridad
2. Recopilar logs relevantes de los últimos 60 minutos
3. Bloquear temporalmente IPs sospechosas mediante Azure Firewall
4. Notificar a oficial de privacidad de datos y oficial de cumplimiento
5. Iniciar preservación de evidencia forense  
**Investigación:**
1. Analizar logs de autenticación para patrón de ataque
2. Revisar cambios recientes en código o configuración
3. Verificar integridad de datos sensibles mediante checksums
4. Consultar con equipo legal según requisitos regulatorios  
**Recuperación:**
1. Implementar controles adicionales basados en hallazgos
2. Comunicar a usuarios afectados según políticas de privacidad
3. Documentar lecciones aprendidas y actualizar plan de respuesta
4. Realizar ejercicio de simulación dentro de 30 días

## Guía de Despliegue

### Requisitos Previos
1. Suscripción de Azure activa con permisos de propietario o colaborador
2. Terraform 1.5+ instalado localmente
3. Azure CLI instalado y autenticado (`az login`)
4. Acceso a Azure Container Registry (se crea durante despliegue)
5. Permisos para crear recursos en grupos de recursos destinados

### Procedimiento de Despliegue Paso a Paso

#### Paso 1: Preparación del Ambiente
```bash
# Clonar repositorio (si no está hecho previamente)
git clone <repository-url>
cd AI-Powered-Fintech-Cloud-Observability-Platform

# Inicializar Terraform
cd terraform
terraform init

# Seleccionar ambiente de destino (dev, staging, prod)
terraform workspace select dev  # o staging o prod
```

#### Paso 2: Revisión y Validación
```bash
# Validar configuración de Terraform
terraform validate

# Formatear código según estándares
terraform fmt

# Revisar plan de cambios
terraform plan -out=tfplan

# Opcional: Guardar plan para revisión por equipo
terraform show -json tfplan > plan.json
```

#### Paso 3: Ejecución del Despliegue
```bash
# Aplicar cambios a infraestructura
terraform apply tfplan

# Esperar completion (tiempo estimado: 15-25 minutos)
# Salida mostrará recursos creados y sus propiedades
```

#### Paso 4: Validación Post-Despliegue
```bash
# Verificar que todos los recursos se crearon correctamente
az resource list --resource-group <resource-group-name> --output table

# Probar health endpoints de servicios desplegados
curl https://<api-endpoint>/api/metrics/health

# Verificar logs iniciales para detectar problemas tempranos
az monitor log-analytics query --workspace <workspace-id> --analytics-query "AzureDiagnostics | where TimeGenerated > ago(10m) | limit 20"
```

#### Paso 5: Configuración Post-Despliegue
```bash
# Configurar DNS personalizado si aplica
# Configurar certificados SSL/TLS personalizados si aplica
# Configurar integraciones externas (puertas de enlace de pago, etc.)
# Ejecutar pruebas de humo para validar flujo completo de usuario
```

### Estrategias de Despliegue Avanzadas

#### Despliegue Blue/Green
1. Crear entorno idéntico con sufijo `-green` mientras `-blue` está en producción
2. Desplegar nueva versión en entorno `-green`
3. Enrutar 100% del tráfico a `-green` después de validación
4. Mantener `-blue` como entorno de reserva por 24 horas
5. Destruir entorno `-blue` después de período de observación

#### Despliegue Canary
1. Desplegar nueva versión en subset pequeño de infraestructura (5-10%)
2. Enrutar 5-10% del tráfico a nueva versión
3. Monitorear métricas clave de salud y negocio
4. Aumentar gradualmente porcentaje de tráfico si métricas son buenas
5. Finalmente llegar a 100% de tráfico en nueva versión

### Procedimientos de Mantenimiento Rutinario

#### Tareas Diarias
- Revisar alertas de monitoreo en Azure Portal
- Verificar health checks de servicios críticos
- Revisar logs de error para patrones recurrentes
- Verificar utilización de recursos (CPU, memoria, red, almacenamiento)

#### Tareas Semanales
- Aplicar actualizaciones de seguridad del sistema operativo
- Rotar credenciales de servicios externos según política
- Limpiar logs antiguos según políticas de retención
- Probar procedimientos de backup y restauración

#### Tareas Mensuales
- Revisar y actualizar políticas de control de acceso
- Revisar efectividad de reglas de rate limiting
- Evaluar necesidad de ajustar tamaños de instancias o niveles de servicio
- Realizar ejercicios de simulación de recuperación ante desastres

### Solución de Problemas Comunes

#### Problema: Alta latencia en respuestas de API
**Posibles causas y soluciones:**
1. **Pool de conexiones agotado:** Aumentar tamaño de connection pool o identificar fugas
2. **Caché ineficiente:** Verificar hit ratio de caché y ajustar TTL o claves de caché
3. **Consultas de base de datos lentas:** Revisar planes de ejecución y considerar índices adicionales
4. **Sobrecarga de CPU:** Escalar verticalmente o horizontalmente según tipo de carga
5. **Latencia de red:** Verificar conexiones entre servicios y considerar colocación geográfica

#### Problema: Fallos en despliegue de Terraform
**Posibles causas y soluciones:**
1. **Conflictos de estado:** Ejecutar `terraform refresh` para reconciliar estado real con estado en archivo
2. **Conflictos de nombres:** Verificar que nombres de recursos sean únicos dentro del grupo de recursos
3. **Limitaciones de cuota:** Verificar límites de suscripción y solicitar aumento si necesario
4. **Problemas de dependencia:** Esperar que recursos dependientes estén completamente provisionados antes de continuar
5. **Credenciales expiradas:** Renovar sesión de Azure CLI con `az login`

#### Problema: Inconsistencia entre caché y fuente de verdad
**Posibles causas y soluciones:**
1. **TTL demasiado alto:** Reducir tiempo de vida para datos que cambian frecuentemente
2. **Invalidación perdida:** Verificar que eventos de actualización desencadenan invalidación de caché
3. **Condiciones de carrera:** Implementar mecanismos de bloqueo o versiones para actualizaciones concurrentes
4. **Fallos silenciosos:** Mejorar logging y alertas para fallos en operaciones de caché

## Glosario de Términos

- **IaC (Infrastructure as Code):** Gestión de infraestructura mediante archivos de definición legibles por máquina
- **PaaS (Platform as a Service):** Modelo de computación en la nube que proporciona una plataforma permettant a los clientes desarrollar, ejecutar y gestionar aplicaciones
- **SaaS (Software as a Service):** Modelo de distribución de licencias en el cual el software se aloja en la nube y se dispone vía internet
- **MTBF (Mean Time Between Failures):** Métrica de fiabilidad que indica el tiempo promedio entre fallos de un sistema
- **MTTR (Mean Time To Recovery):** Métrica que mide el tiempo promedio necesario para recuperar un sistema después de un fallo
- **SLA (Service Level Agreement):** Contrato entre proveedor de servicio y cliente que define el nivel de servicio esperado
- **KPI (Key Performance Indicator):** Medida cuantificable utilizada para evaluar el éxito en el cumplimiento de objetivos
- **OKR (Objectives and Key Results):** Metodología de establecimiento de objetivos que define objetivos y resultados medibles para alcanzar esos objetivos