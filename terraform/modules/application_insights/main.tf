resource "azurerm_application_insights" "main" {
  name                = "appi-${local.name_prefix}"
  location            = var.location
  resource_group_name = var.resource_group_name
  application_type    = "web"
  retention_in_days   = 90
  tags                = var.tags

  # workspace_id is required for classic (non-container) App Insights.
  # The resource was created by Azure with an auto-generated workspace,
  # so we accept it as an optional variable. If not provided, it stays null.
  # Note: once set, workspace_id cannot be removed — this is by Azure design.
  workspace_id = var.workspace_id
}
