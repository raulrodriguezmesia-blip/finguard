# Spec: Despliegue de Infraestructura con Terraform en Azure (AKS/Container Apps)

## Objective
Desplegar la plataforma AI-Powered Fintech & Cloud Observability en Azure usando Infrastructure as Code (IaC) con Terraform. La solución proporcionará despliegues consistentes, reproducibles y versionados para todos los componentes de la plataforma incluyendo servicios de Kubernetes (AKS), contenedores (Container Apps), redes, almacenamiento y recursos de monitoreo.

## Tech Stack
- Terraform 1.5+ para Infrastructure as Code
- Azure Provider para Terraform
- AKS (Azure Kubernetes Service) para orquestación de contenedores
- Azure Container Apps para servicios sin servidor
- Azure Redis Cache para capa de caché
- Azure PostgreSQL Flexible Server para bases de datos relacionales
- Azure Monitor y Application Insights para observabilidad
- Azure Key Vault para manejo de secrets
- Azure Container Registry para almacenamiento de imágenes de contenedor

## Commands
- Inicializar Terraform: `terraform init`
- Validar configuración: `terraform validate`
- Crear plan de ejecución: `terraform plan`
- Aplicar cambios: `terraform apply`
- Destruir infraestructura: `terraform destroy`
- Importar recursos existentes: `terraform import`
- Formatear código: `terraform fmt`
- Validar sintaxis: `terraform validate`

## Project Structure
```
terraform/                    → Código de Terraform para infraestructura
  ├── modules/                → Módulos reutilizables de Terraform
  │   ├── aks/                → Módulo para clúster AKS
  │   ├── container-apps/     → Módulo para Azure Container Apps
  │   ├── networking/         → Módulo para VNet, subnets, NSG
  │   ├── database/           → Módulo para Azure PostgreSQL
  │   ├── cache/              → Módulo para Azure Redis
  │   ├── monitoring/         → Módulo para Azure Monitor y Log Analytics
  │   └── security/           → Módulo para Key Vault y políticas de acceso
  │
  ├── environments/           → Configuraciones específicas por entorno
  │   ├── dev/                → Configuración de desarrollo
  │   ├── staging/            → Configuración de staging
  │   └── prod/               → Configuración de producción
  │
  ├── main.tf                 → Configuración principal
  ├── variables.tf            → Variables de entrada
  ├── outputs.tf              → Valores de salida
  ├── versions.tf             → Versiones de Terraform y providers
  └── backend.tf              → Configuración de backend para estado
  
  scripts/                    → Scripts de ayuda y automatización
    ├── deploy.sh             → Script de despliegue completo
    ├── validate.sh           → Script de validación previa al despliegue
    └── destroy.sh            → Script de destrucción controlada
```

## Code Style
- Archivos de Terraform con extensión `.tf`
- Variables descritas claramente con tipos y descripciones
- Recursos nombrados siguiendo convención: `<tipo>-<propósito>-<ambiente>`
- Comentarios explicativos para recursos complejos o configuraciones especiales
- Archivos organizados lógicamente por tipo de recurso y función
- Uso consistente de interpolación de variables `${var.nombre}` y `${recurso.atributo}`
- Condiciones ternarias para lógica simple: `condition ? valor_si_verdadero : valor_si_falso`
- Bloques `dynamic` para recursos que se repiten con variaciones mínimas

## Testing Strategy
- Pruebas de sintaxis: `terraform validate` en CI
- Pruebas de formato: `terraform fmt -check` en CI
- Pruebas de plan en entornos aislados: workspace de Terraform separado para testing
- Pruebas de integración limitadas debido a naturaleza de infraestructura (costo y tiempo)
- Validación de outputs mediante scripts de prueba después del apply
- Revisión manual de planos antes de aplicar en producción

## Boundaries
- **Always:** Ejecutar `terraform fmt` y `terraform validate` antes de commits, usar workspaces separados para entornos, revisar planos antes de aplicar
- **Ask first:** Cambios en tipos de instancia de VM, cambios significativos en topología de red, actualizaciones de versiones de providers mayores
- **Never:** Commit de secrets en archivos de Terraform, aplicar cambios directamente a prod sin revisión de plano, destruir recursos en prod sin confirmación explícita

## Success Criteria
- Infraestructura completa desplegada en menos de 25 minutos con un solo comando
- Todos los servicios accesibles y respondiendo health checks después del despliegue
- Estado de Terraform almacenado de forma segura en backend remoto (Azure Storage Account)
- Capacidad de destruir y recrear infraestructura idempotente
- Costos mensuales estimados visibles y dentro del presupuesto definido
- Separación clara entre entornos (dev, staging, prod) sin riesgo de contaminación

## Open Questions
- ¿Prefieren usar exclusivamente AKS o una combinación de AKS y Container Apps según tipo de servicio?
- ¿Qué tamaño y configuración de nodos de AKS consideran apropiados para la carga esperada?
- ¿Requieren alta disponibilidad específica (zonas de disponibilidad, conjuntos de escalado) para ciertos componentes?
- ¿Qué estrategia de manejo de secrets prefieren (Key Vault integrado, secrets de Kubernetes, o combinación)?
- ¿Necesitan configuraciones específicas de red (peering, expres routes, VPN) para conectar con sistemas existentes?