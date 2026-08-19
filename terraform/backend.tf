terraform {
  backend "azurerm" {
    resource_group_name  = "rg-finguard-terraform-state"
    storage_account_name = "finguardtfstate"
    container_name       = "tfstate"
    key                  = "dev/terraform.tfstate"
  }
}
