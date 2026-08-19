# Terraform - FinGuard (Azure)

Infraestructura como código para desplegar FinGuard en **Azure AKS** con mejores prácticas senior: managed identity, private endpoints, autoscaling, observabilidad y control de costo por ambiente.

## Estructura

```
terraform/
├── main.tf                        # Orquestación de módulos
├── variables.tf                    # Variables globales
├── outputs.tf                      # Outputs globales
├── providers.tf                    # Provider Azure
├── backend.tf                      # Backend remoto por defecto (dev)
├── backend-dev.hcl                 # Backend config para dev
├── backend-staging.hcl             # Backend config para staging
├── backend-prod.hcl                # Backend config para prod
├── terraform.tfvars.example        # Variables de ejemplo
└── modules/
    ├── bootstrap/                  # RG + Storage Account para estado
    ├── vnet/                       # VNet, subnets, NSGs
    ├── aks/                        # Cluster AKS con node pools y autoscaling
    ├── acr/                        # Azure Container Registry
    ├── log_analytics/              # Workspace para Container Insights
    ├── key_vault/                  # Key Vault para secrets
    └── application_insights/       # APM para Spring Boot
```

## Prerrequisitos

- Terraform >= 1.5
- Azure CLI (`az login`)
- Permisos de Owner o Contributor en la suscripción

## Uso

### 1. Inicializar backend (solo una vez)

```bash
# Crear resource group y storage account para estado de Terraform
terraform init
terraform apply -target=module.bootstrap -var-file=terraform.tfvars
```

### 2. Inicializar proyecto

```bash
terraform init -backend-config=backend-dev.hcl
```

### 3. Planificar y aplicar

```bash
terraform plan -var-file=terraform.tfvars
terraform apply -var-file=terraform.tfvars
```

### 4. Conectar al cluster

```bash
az aks get-credentials --resource-group $(terraform output -raw resource_group_name) --name $(terraform output -raw aks_cluster_name)
```

## Ambientes

| Ambiente | Backend | VM Size | Nodos | ACR SKU | Retención logs |
|----------|---------|---------|-------|---------|----------------|
| dev | `backend-dev.hcl` | `Standard_B2s` | 1-3 | Basic | 7 días |
| staging | `backend-staging.hcl` | `Standard_D2s_v3` | 2-5 | Standard | 14 días |
| prod | `backend-prod.hcl` | `Standard_D4s_v3` | 3-10 | Standard | 30 días |

## Control de costo

- **Spot/Preemptible nodes**: habilitar en `aks` module para dev/staging
- **Autoscaling**: cluster autoscaler + HPA por defecto
- **Burstable VMs**: B-series en dev para cargas intermitentes
- **ACR Basic/Standard**: sin geo-replicación en dev

## Seguridad

- **Managed Identity** para AKS (sin service principals)
- **Key Vault** para secrets con acceso por identidad administrada
- **Azure AD integration** para RBAC de Kubernetes
- **Network Policies** para aislamiento de namespaces
- **Private Endpoints** para ACR y Key Vault en prod
- **Azure Policy** para cumplimiento

## Observabilidad

- **Azure Monitor / Container Insights** para métricas de nodos y pods
- **Application Insights** para tracing de la aplicación Spring Boot
- **Log Analytics** para logs centralizados

## Notas

- El módulo `bootstrap` debe aplicarse una sola vez por suscripción/región.
- Para producción, cambiar `node_pool_vm_size` y habilitar `private_cluster_enabled`.
- Usar `terraform plan` antes de cualquier `apply`.
