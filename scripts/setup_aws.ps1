# FinGuard - AWS Bootstrap Script
# Crea recursos necesarios para Terraform remote state (S3 + DynamoDB)
# Uso: .\setup_aws.ps1 -Region us-east-1 -BucketName finguard-terraform-state -TableName finguard-terraform-locks

param(
    [string]$Region = "us-east-1",
    [string]$BucketName = "finguard-terraform-state",
    [string]$TableName = "finguard-terraform-locks",
    [string]$Profile = "default"
)

$ErrorActionPreference = "Stop"

Write-Host "=== FinGuard AWS Bootstrap ===" -ForegroundColor Cyan
Write-Host "Region: $Region"
Write-Host "Bucket: $BucketName"
Write-Host "Table:  $TableName"
Write-Host ""

# Crear bucket S3 para Terraform state
Write-Host "[1/3] Creando bucket S3: $BucketName..." -ForegroundColor Yellow
try {
    $bucketExists = aws s3 ls "s3://$BucketName" --region $Region --profile $Profile 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "  Bucket ya existe." -ForegroundColor Green
    } else {
        aws s3 mb "s3://$BucketName" --region $Region --profile $Profile
        Write-Host "  Bucket creado." -ForegroundColor Green
    }
} catch {
    Write-Host "  Error: $_" -ForegroundColor Red
    exit 1
}

# Habilitar versionado y cifrado en el bucket
Write-Host "[2/3] Configurando bucket S3 (versionado + cifrado)..." -ForegroundColor Yellow
try {
    aws s3api put-bucket-versioning --bucket $BucketName --versioning-configuration Status=Enabled --region $Region --profile $Profile | Out-Null
    aws s3api put-bucket-encryption --bucket $BucketName --server-side-encryption-configuration '{"Rules":[{"ApplyServerSideEncryptionByDefault":{"SSEAlgorithm":"AES256"}}]}' --region $Region --profile $Profile | Out-Null
    Write-Host "  Configuración aplicada." -ForegroundColor Green
} catch {
    Write-Host "  Error: $_" -ForegroundColor Red
    exit 1
}

# Crear tabla DynamoDB para lock de Terraform
Write-Host "[3/3] Creando tabla DynamoDB: $TableName..." -ForegroundColor Yellow
try {
    $tableExists = aws dynamodb describe-table --table-name $TableName --region $Region --profile $Profile 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "  Tabla ya existe." -ForegroundColor Green
    } else {
        aws dynamodb create-table --table-name $TableName --attribute-definitions AttributeName=LockID,AttributeType=S --key-schema AttributeName=LockID,KeyType=HASH --billing-mode PAY_PER_REQUEST --region $Region --profile $Profile | Out-Null
        Write-Host "  Tabla creada." -ForegroundColor Green
    }
} catch {
    Write-Host "  Error: $_" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "=== Bootstrap completado ===" -ForegroundColor Cyan
Write-Host "Ahora puedes ejecutar:"
Write-Host "  terraform init -backend-config=terraform/backend-dev.hcl"
