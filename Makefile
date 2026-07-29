.PHONY: help build test verify docker-build docker-run terraform-init terraform-plan terraform-apply clean

help:
	@echo "FinGuard - Comandos disponibles:"
	@echo "  build           - Compilar el proyecto con Maven"
	@echo "  test            - Ejecutar tests unitarios"
	@echo "  verify          - Compilar y ejecutar todos los tests"
	@echo "  docker-build    - Construir imagen Docker"
	@echo "  docker-run      - Ejecutar contenedor Docker localmente"
	@echo "  docker-compose  - Levantar stack completo local (PostgreSQL + Kafka + app)"
	@echo "  terraform-init  - Inicializar Terraform (dev)"
	@echo "  terraform-plan  - Planificar cambios en Terraform (dev)"
	@echo "  terraform-apply - Aplicar cambios en Terraform (dev)"
	@echo "  clean           - Limpiar build artifacts"
	@echo "  setup-aws       - Ejecutar bootstrap de AWS (S3 + DynamoDB)"
	@echo "  deploy          - Deploy completo a AWS (build + push ECR + Terraform)"

build:
	mvn clean package -DskipTests

test:
	mvn test

verify:
	mvn verify

docker-build:
	docker build -t finguard:latest .

docker-run:
	docker run -p 8080:8080 finguard:latest

docker-compose:
	docker-compose up --build

terraform-init:
	cd terraform && terraform init -backend-config=backend-dev.hcl

terraform-plan:
	cd terraform && terraform plan -var-file=terraform.tfvars

terraform-apply:
	cd terraform && terraform apply -var-file=terraform.tfvars

clean:
	mvn clean
	Remove-Item -Recurse -Force terraform/.terraform -ErrorAction SilentlyContinue
	Remove-Item -Recurse -Force target -ErrorAction SilentlyContinue

setup-aws:
	pwsh scripts/setup_aws.ps1

deploy:
	pwsh scripts/deploy.ps1 -Environment dev
