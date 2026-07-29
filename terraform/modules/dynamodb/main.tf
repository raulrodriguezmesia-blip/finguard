variable "name_prefix" {
  type = string
}

variable "environment" {
  type    = string
  default = "prod"
}

resource "aws_dynamodb_table" "feature_store" {
  name         = "${var.name_prefix}-feature-store"
  billing_mode = var.billing_mode
  hash_key     = "customerId"

  attribute {
    name = "customerId"
    type = "S"
  }

  ttl {
    attribute_name = "ttl"
    enabled        = true
  }

  tags = {
    Name        = "${var.name_prefix}-feature-store"
    Environment = var.environment
  }
}

output "feature_store_table_name" {
  value = aws_dynamodb_table.feature_store.name
}

output "feature_store_table_arn" {
  value = aws_dynamodb_table.feature_store.arn
}
