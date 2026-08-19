resource_group_name  = "rg-finguard-terraform-state"
storage_account_name = "finguardtfstate"
container_name       = "tfstate"
key                  = "staging/terraform.tfstate"
use_azuread_auth     = true
