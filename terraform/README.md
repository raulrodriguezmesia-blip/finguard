# Terraform - FinGuard

Esta carpeta contiene la infraestructura como código para desplegar FinGuard en AWS.

## Estructura

```
terraform/
├── main.tf                 # Punto de entrada: configura provider y llama a módulos
├── variables.tf            # Variables globales del proyecto
├── outputs.tf              # Outputs globales
├── providers.tf            # Configuración del provider AWS
├── backend-*.hcl           # Backends remotos por ambiente (dev, staging, prod)
├── terraform.tfvars.example # Variables de ejemplo
├── modules/
│   ├── vpc/                # VPC, subnets, security groups, IGW, NAT
│   ├── iam/                # Roles ECS, SageMaker, policies
│   ├── sns/                # Topic para alertas de fraude
│   ├── dynamodb/           # Feature store
│   ├── aurora/             # Cluster Aurora PostgreSQL
│   ├── ecs_fargate/        # Cluster ECS, task definition, ALB, service
│   ├── sagemaker/          # Modelo, endpoint config, endpoint
│   └── bootstrap/          # S3 state bucket + DynamoDB lock table
└── scripts/
    └── generate_sagemaker_model.py  # Genera modelo dummy para SageMaker
```

## Prerrequisitos

- Terraform >= 1.5
- AWS CLI configurado con credenciales
- Bucket S3 y tabla DynamoDB para estado remoto (se crean con el módulo `bootstrap`)

## Uso

```bash
# Inicializar Terraform (descarga providers)
terraform init -backend-config=backend-dev.hcl

# Planificar cambios
terraform plan -var-file=terraform.tfvars

# Aplicar cambios
terraform apply -var-file=terraform.tfvars
```

## Backend remoto

El estado de Terraform se almacena en S3 con bloqueo por DynamoDB. Los archivos `backend-*.hcl` definen el bucket y tabla por ambiente.

- `backend-dev.hcl`
- `backend-staging.hcl`
- `backend-prod.hcl`

**Importante**: crear el bucket S3 y la tabla DynamoDB manualmente la primera vez, o ejecutar el módulo `bootstrap` con un proveedor sin backend:

```bash
terraform init
terraform apply -target=module.bootstrap -var-file=terraform.tfvars
```

## Variables sensibles

Las contraseñas y secrets deben manejarse con cuidado. Usar:

- Variables de entorno (`TF_VAR_db_password`)
- Secrets Manager de AWS
- Terraform Cloud variables

## Notas

- El módulo `bootstrap` debe aplicarse una sola vez por cuenta/región.
- Para producción, cambiar `desired_count` y recursos de base de datos a instancias mayores.
- El ALB está expuesto a Internet; considerar WAF para protección adicional.
