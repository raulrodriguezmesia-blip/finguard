resource "azurerm_resource_group" "main" {
  name     = "rg-${var.project_name}-${var.environment}"
  location = var.location
  tags     = var.tags
}

resource "azurerm_storage_account" "tfstate" {
  name                     = replace("${var.project_name}tfstate", "-", "")
  resource_group_name      = var.resource_group_name
  location                 = var.location
  account_tier             = "Standard"
  account_replication_type = "LRS"
  min_tls_version          = "TLS1_2"
  tags                     = var.tags

  blob_properties {
    delete_retention_policy {
      days = 30
    }
    container_delete_retention_policy {
      days = 30
    }
  }
}

# Container "tfstate" and table "terraformlocks" already exist in the
# storage account and are managed externally by the Terraform backend.
# They are intentionally NOT managed here to avoid create-conflicts.
# If bootstrapping from scratch, uncomment the two blocks below.

# resource "azurerm_storage_container" "tfstate" {
#   name                  = "tfstate"
#   storage_account_name  = azurerm_storage_account.tfstate.name
#   container_access_type = "private"
# }

# resource "azurerm_storage_table" "locks" {
#   name                 = "terraformlocks"
#   storage_account_name = azurerm_storage_account.tfstate.name
# }
