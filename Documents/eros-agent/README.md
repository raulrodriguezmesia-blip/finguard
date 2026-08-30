# Eros Code Analysis Agent

Agente de análisis proactivo de código con IA, integrado en pipelines CI/CD para detectar y prevenir vulnerabilidades de seguridad antes del despliegue.

## Descripción

Eros es un agente de IA con razonamiento estructurado en 5 fases que analiza código, detecta vulnerabilidades OWASP Top 10, y genera código seguro automáticamente. Se integra como guardián en CI/CD para bloquear despliegues inseguros.

## Arquitectura

```
┌─────────────────────────────────────────────────────────────┐
│                    Pipeline CI/CD                            │
├─────────────────────────────────────────────────────────────┤
│  Commit → Build → Test → EROS GATE → Deploy → ✅ SEGURO    │
│                            ↓                                │
│                      ❌ BLOQUEA si hay vulnerabilidades     │
└─────────────────────────────────────────────────────────────┘

Fases de Análisis:
1. Syntax Check    (45ms)   - Valida AST
2. Quality Review  (120ms)  - Evalúa estándares
3. Security Audit  (350ms)  - Escanea OWASP Top 10
4. Performance     (80ms)   - Encuentra cuellos de botella
5. Refactoring     (405ms)  - Genera código seguro
                     ↓
                Total: ~1 seg, 98% precisión
```

## Requisitos

- Java 17+
- Maven 3.8+
- Docker
- Azure Kubernetes Service (AKS)
- Azure Container Registry (ACR)

## Instalación Local

```bash
# Clonar repositorio
git clone https://github.com/raulrodriguezmesia-blip/eros-agent.git
cd eros-agent

# Compilar
mvn clean install

# Ejecutar localmente
mvn spring-boot:run
```

## Uso

### Analizar código

```bash
curl -X POST http://localhost:8080/api/analyze \
  -H "Content-Type: application/json" \
  -d '{
    "code": "def get_user(user_id): return db.execute(f\"SELECT * FROM users WHERE id={user_id}\")",
    "language": "python"
  }'
```

### Respuesta

```json
{
  "status": "completed",
  "findings": [
    {
      "phase": "security_audit",
      "severity": "CRITICAL",
      "cwe": "CWE-89",
      "description": "SQL Injection vulnerability detected",
      "line": 1,
      "fix": "Use parameterized queries"
    }
  ],
  "risk_level": "HIGH"
}
```

## Despliegue en Azure

### 1. Construir imagen Docker

```bash
docker build -t eros-agent:latest .
```

### 2. Subir a Azure Container Registry

```bash
az acr login --name <acr-name>
docker tag eros-agent:latest <acr-name>.azurecr.io/eros-agent:latest
docker push <acr-name>.azurecr.io/eros-agent:latest
```

### 3. Desplegar en AKS

```bash
# Obtener credenciales
az aks get-credentials --resource-group <rg-name> --name <aks-name>

# Aplicar manifests
kubectl apply -f k8s/

# Verificar
kubectl get pods -n finguard
kubectl get svc -n finguard
```

## Pipeline CI/CD

El pipeline de GitHub Actions (`.github/workflows/ci.yml`) ejecuta automáticamente:

1. **Build**: Compila el código Java con Maven
2. **Test**: Ejecuta tests unitarios y de integración
3. **Security Scan**: Bandit + Safety para dependencias
4. **Docker Build**: Construye imagen Docker
5. **Push a ACER**: Sube imagen al registro privado
6. **Deploy a AKS**: Actualiza el deployment

## Observabilidad

### Endpoints Actuator

| Endpoint | Descripción |
|----------|-------------|
| `/actuator/health` | Health check |
| `/actuator/info` | Info de la aplicación |
| `/actuator/metrics` | Métricas Prometheus |
| `/actuator/prometheus` | Métricas formato Prometheus |

### Métricas Custom

| Métrica | Descripción |
|---------|-------------|
| `eros_analysis_total` | Total de análisis ejecutados |
| `eros_vulnerabilities_found` | Vulnerabilidades detectadas |
| `eros_analysis_duration_seconds` | Duración del análisis |

## Estructura del Proyecto

```
eros-agent/
├── README.md                 # Este archivo
├── pom.xml                   # Configuración Maven
├── Dockerfile                # Imagen Docker
├── .github/workflows/ci.yml  # Pipeline CI/CD
├── docs/                     # Documentación
│   └── architecture.png
├── src/
│   ├── main/
│   │   ├── java/com/eros/agent/
│   │   │   ├── Application.java
│   │   │   ├── config/
│   │   │   │   └── SecurityConfig.java
│   │   │   ├── security/
│   │   │   │   ├── VulnerabilityScanner.java
│   │   │   │   └── SecurityRule.java
│   │   │   └── analysis/
│   │   │       ├── AnalysisController.java
│   │   │       ├── AnalysisService.java
│   │   │       └── AnalysisResult.java
│   │   └── resources/
│   │       └── application.yml
│   └── test/
│       └── java/com/eros/agent/
│           └── AnalysisServiceTest.java
└── k8s/
    ├── deployment.yaml       # Deployment AKS
    ├── service.yaml          # Servicio LoadBalancer
    └── hpa.yaml              # Autoscaling
```

## Autor

**Raul Rodriguez Mesia**  
Frontier Engineering Challenge 2026 — micro1

## Licencia

MIT License
