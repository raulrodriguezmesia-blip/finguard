resource "azurerm_container_registry" "main" {
  name                = replace("${local.name_prefix}acr", "-", "")
  location            = var.location
  resource_group_name = var.resource_group_name
  sku                 = var.sku
  tags                = var.tags

  admin_enabled = false

  identity {
    type = "SystemAssigned"
  }
}
