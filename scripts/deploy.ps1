# FinGuard - Deploy Script
# Automatiza build, push a ECR y apply de Terraform
# Uso: .\deploy.ps1 -Environment dev -ImageTag latest

param(
    [ValidateSet("dev", "staging", "prod")]
    [string]$Environment = "dev",
    [string]$ImageTag = "latest",
    [string]$AwsRegion = "us-east-1",
    [string]$EcrRepository = "finguard",
    [string]$Profile = "default"
)

$ErrorActionPreference = "Stop"

$rootDir = Split-Path -Parent $PSScriptRoot
$terraformDir = Join-Path $rootDir "terraform"
$backendFile = Join-Path $terraformDir "backend-$Environment.hcl"
$tfvarsFile = Join-Path $terraformDir "terraform.tfvars"

Write-Host "=== FinGuard Deploy ($Environment) ===" -ForegroundColor Cyan
Write-Host "Region:      $AwsRegion"
Write-Host "Environment: $Environment"
Write-Host "Image Tag:   $ImageTag"
Write-Host ""

# Validar prerequisitos
Write-Host "[0/4] Validando prerequisitos..." -ForegroundColor Yellow
if (-not (Get-Command aws -ErrorAction SilentlyContinue)) {
    Write-Host "  ERROR: AWS CLI no encontrado." -ForegroundColor Red
    exit 1
}
if (-not (Get-Command terraform -ErrorAction SilentlyContinue)) {
    Write-Host "  ERROR: Terraform no encontrado." -ForegroundColor Red
    exit 1
}
if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
    Write-Host "  ERROR: Docker no encontrado." -ForegroundColor Red
    exit 1
}
if (-not (Test-Path $backendFile)) {
    Write-Host "  ERROR: Backend file not found: $backendFile" -ForegroundColor Red
    exit 1
}
Write-Host "  Prerequisitos OK." -ForegroundColor Green

# Login en ECR
Write-Host "[1/4] Login en Amazon ECR..." -ForegroundColor Yellow
$ecrLogin = aws ecr get-login-password --region $AwsRegion --profile $Profile | docker login --username AWS --password-stdin "$AwsRegion.dkr.ecr.$AwsRegion.amazonaws.com"
if ($LASTEXITCODE -ne 0) {
    Write-Host "  ERROR en login ECR." -ForegroundColor Red
    exit 1
}
Write-Host "  Login exitoso." -ForegroundColor Green

# Obtener registry URL
$accountId = aws sts get-caller-identity --query Account --output text --region $AwsRegion --profile $Profile
if ($LASTEXITCODE -ne 0) {
    Write-Host "  ERROR: No se pudo obtener Account ID de AWS." -ForegroundColor Red
    exit 1
}
$registryUrl = "$accountId.dkr.ecr.$AwsRegion.amazonaws.com"
$imageFull = "$registryUrl/$EcrRepository`:$ImageTag"

# Build y push de imagen Docker
Write-Host "[2/4] Build y push de imagen Docker: $imageFull" -ForegroundColor Yellow
Push-Location $rootDir
try {
    docker build -t $imageFull .
    docker push $imageFull
    Write-Host "  Imagen push exitoso." -ForegroundColor Green
} catch {
    Write-Host "  ERROR en build/push: $_" -ForegroundColor Red
    exit 1
} finally {
    Pop-Location
}

# Terraform init
Write-Host "[3/4] Terraform init..." -ForegroundColor Yellow
Push-Location $terraformDir
try {
    terraform init -backend-config=$backendFile
    Write-Host "  Terraform init exitoso." -ForegroundColor Green
} catch {
    Write-Host "  ERROR en terraform init: $_" -ForegroundColor Red
    exit 1
} finally {
    Pop-Location
}

# Terraform plan y apply
Write-Host "[4/4] Terraform plan y apply..." -ForegroundColor Yellow
Push-Location $terraformDir
try {
    $planOutput = terraform plan -var="container_image=$imageFull" -out=tfplan
    if ($LASTEXITCODE -ne 0) {
        Write-Host "  ERROR en terraform plan." -ForegroundColor Red
        exit 1
    }
    
    if ($Environment -eq "prod") {
        Write-Host "  Ambiente PROD requiere aprobación manual." -ForegroundColor Yellow
        $confirm = Read-Host "  ¿Aplicar cambios en PROD? (y/N)"
        if ($confirm -ne "y" -and $confirm -ne "Y") {
            Write-Host "  Apply cancelado por usuario." -ForegroundColor Yellow
            exit 0
        }
    }
    
    terraform apply -auto-approve tfplan
    Write-Host "  Terraform apply exitoso." -ForegroundColor Green
} catch {
    Write-Host "  ERROR en terraform apply: $_" -ForegroundColor Red
    exit 1
} finally {
    Pop-Location
}

Write-Host ""
Write-Host "=== Deploy completado ===" -ForegroundColor Cyan
Write-Host "Verificar en AWS Console:"
Write-Host "  - ECS Service"
Write-Host "  - ALB DNS"
Write-Host "  - CloudWatch Metrics"
Write-Host ""
terraform output -json | ConvertFrom-Json | ForEach-Object {
    $_.PSObject.Properties | ForEach-Object {
        Write-Host "$($_.Name): $($_.Value.value)"
    }
}
