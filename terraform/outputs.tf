output "resource_group_name" {
  description = "Nombre del resource group principal"
  value       = local.bootstrap_rg_name
}

output "aks_cluster_name" {
  description = "Nombre del cluster AKS"
  value       = module.aks.aks_cluster_name
}

output "aks_cluster_fqdn" {
  description = "FQDN del API server de AKS"
  value       = module.aks.aks_cluster_fqdn
}

output "aks_kubeconfig" {
  description = "Comando para obtener kubeconfig"
  value       = "az aks get-credentials --resource-group ${local.bootstrap_rg_name} --name ${module.aks.aks_cluster_name}"
  sensitive   = true
}

output "acr_login_server" {
  description = "Login server de ACR"
  value       = module.acr.acr_login_server
}

output "acr_id" {
  description = "ID del recurso ACR"
  value       = module.acr.acr_id
}

output "key_vault_uri" {
  description = "URI de Key Vault"
  value       = module.key_vault.key_vault_uri
}

output "log_analytics_workspace_id" {
  description = "ID del workspace de Log Analytics"
  value       = module.log_analytics.workspace_id
}

output "application_insights_connection_string" {
  description = "Connection string de Application Insights"
  value       = module.application_insights.connection_string
  sensitive   = true
}

output "vnet_id" {
  description = "ID de la VNet"
  value       = module.vnet.vnet_id
}
