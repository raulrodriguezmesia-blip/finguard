variable "project_name" {
  description = "Nombre del proyecto"
  type        = string
  default     = "finguard"
}

variable "environment" {
  description = "Ambiente (dev, staging, prod)"
  type        = string
  default     = "prod"
}

variable "aws_region" {
  description = "Región de AWS"
  type        = string
  default     = "us-east-1"
}

variable "vpc_cidr" {
  description = "CIDR block para la VPC"
  type        = string
  default     = "10.0.0.0/16"
}

variable "availability_zones" {
  description = "AZs disponibles en la región"
  type        = list(string)
  default     = ["us-east-1a", "us-east-1b", "us-east-1c"]
}

variable "public_subnets" {
  description = "CIDR blocks para subnets públicas"
  type        = list(string)
  default     = ["10.0.1.0/24", "10.0.2.0/24", "10.0.3.0/24"]
}

variable "private_subnets" {
  description = "CIDR blocks para subnets privadas"
  type        = list(string)
  default     = ["10.0.11.0/24", "10.0.12.0/24", "10.0.13.0/24"]
}

variable "db_username" {
  description = "Usuario de la base de datos"
  type        = string
  default     = "finguard"
}

variable "db_password" {
  description = "Contraseña de la base de datos"
  type        = string
  sensitive   = true
  default     = "changeme" # Cambiar en producción
}

variable "container_image" {
  description = "Imagen Docker del backend"
  type        = string
  default     = "public.ecr.aws/aws/amazon-linux:latest" # Placeholder
}

variable "desired_count" {
  description = "Número de tareas ECS deseadas"
  type        = number
  default     = 2
}

variable "sagemaker_s3_bucket" {
  description = "Bucket S3 para artefactos de SageMaker"
  type        = string
  default     = "finguard-sagemaker-artifacts"
}

variable "secrets_manager_arn" {
  description = "ARN del secreto de base de datos en AWS Secrets Manager"
  type        = string
  default     = ""
}
