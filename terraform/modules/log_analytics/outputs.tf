output "workspace_id" {
  value = azurerm_log_analytics_workspace.main.workspace_id
}

output "workspace_resource_id" {
  value = azurerm_log_analytics_workspace.main.id
}

output "workspace_key" {
  value     = azurerm_log_analytics_workspace.main.primary_shared_key
  sensitive = true
}
