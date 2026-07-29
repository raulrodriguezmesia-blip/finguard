variable "name_prefix" {
  type = string
}

variable "environment" {
  type    = string
  default = "prod"
}

resource "aws_sns_topic" "fraud_alerts" {
  name = "${var.name_prefix}-fraud-alerts"

  tags = {
    Name        = "${var.name_prefix}-fraud-alerts"
    Environment = var.environment
  }
}

resource "aws_sns_topic_subscription" "email_example" {
  topic_arn = aws_sns_topic.fraud_alerts.arn
  protocol  = "email"
  endpoint  = "security@example.com"
}

output "alert_topic_arn" {
  value = aws_sns_topic.fraud_alerts.arn
}
