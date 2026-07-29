variable "name_prefix" {
  type = string
}

variable "environment" {
  type    = string
  default = "prod"
}

variable "role_arn" {
  type = string
}

variable "instance_type" {
  type    = string
  default = "ml.t2.medium"
}

variable "s3_bucket" {
  type = string
}

# Bucket S3 para artefactos
resource "aws_s3_bucket" "artifacts" {
  bucket = var.s3_bucket

  tags = {
    Name        = var.s3_bucket
    Environment = var.environment
  }
}

resource "aws_s3_bucket_versioning" "artifacts" {
  bucket = aws_s3_bucket.artifacts.id
  versioning_configuration {
    status = "Enabled"
  }
}

# Modelo de SageMaker
resource "aws_sagemaker_model" "fraud_detection" {
  name               = "${var.name_prefix}-fraud-model"
  execution_role_arn = var.role_arn

  primary_container {
    image          = "683313688378.dkr.ecr.us-east-1.amazonaws.com/sagemaker-scikit-learn:1.0-1-cpu-py310"
    model_data_url = "s3://${aws_s3_bucket.artifacts.id}/models/fraud-detection-v1/model.tar.gz"
  }

  tags = {
    Name        = "${var.name_prefix}-fraud-model"
    Environment = var.environment
  }
}

resource "aws_sagemaker_endpoint_config" "fraud_detection" {
  name = "${var.name_prefix}-endpoint-config"

  production_variants {
    variant_name           = "AllTraffic"
    model_name             = aws_sagemaker_model.fraud_detection.name
    instance_type          = var.instance_type
    initial_instance_count = 1
  }

  tags = {
    Name        = "${var.name_prefix}-endpoint-config"
    Environment = var.environment
  }
}

resource "aws_sagemaker_endpoint" "fraud_detection" {
  name                 = "${var.name_prefix}-endpoint"
  endpoint_config_name = aws_sagemaker_endpoint_config.fraud_detection.name

  tags = {
    Name        = "${var.name_prefix}-endpoint"
    Environment = var.environment
  }
}

output "sagemaker_bucket" {
  value = aws_s3_bucket.artifacts.id
}

output "sagemaker_endpoint_name" {
  value = aws_sagemaker_endpoint.fraud_detection.name
}

output "sagemaker_endpoint_arn" {
  value = aws_sagemaker_endpoint.fraud_detection.arn
}
