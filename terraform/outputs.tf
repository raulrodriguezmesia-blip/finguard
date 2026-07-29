output "alb_dns_name" {
  description = "DNS name del Application Load Balancer"
  value       = module.ecs_fargate.alb_dns_name
}

output "ecs_cluster_name" {
  description = "Nombre del cluster ECS"
  value       = module.ecs_fargate.ecs_cluster_name
}

output "aurora_endpoint" {
  description = "Endpoint de Aurora PostgreSQL"
  value       = module.aurora.aurora_endpoint
  sensitive   = true
}

output "feature_store_table" {
  description = "Nombre de la tabla DynamoDB de feature store"
  value       = module.dynamodb.feature_store_table_name
}

output "sagemaker_endpoint" {
  description = "Nombre del endpoint de SageMaker"
  value       = try(module.sagemaker[0].sagemaker_endpoint_name, "")
}

output "sagemaker_endpoint_arn" {
  description = "ARN del endpoint de SageMaker"
  value       = try(module.sagemaker[0].sagemaker_endpoint_arn, "")
}

output "alert_topic_arn" {
  description = "ARN del topic SNS para alertas"
  value       = module.sns.alert_topic_arn
}
