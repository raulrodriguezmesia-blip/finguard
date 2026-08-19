locals {
  name_prefix = "${var.project_name}-${var.environment}"
  common_tags = merge(var.tags, {
    environment = var.environment
  })
}

# Bootstrap module: creates the resource group and storage account for Terraform state.
# The storage account (finguardtfstate) already exists in rg-finguard-terraform-state
# and was imported into state. We point the module at that existing storage RG.
module "bootstrap" {
  source = "./modules/bootstrap"

  location            = var.location
  project_name        = var.project_name
  environment         = var.environment
  resource_group_name = "rg-finguard-terraform-state"
  tags                = local.common_tags
}

locals {
  bootstrap_rg_name = "rg-finguard-${var.environment}"
  tfstate_sa_name   = module.bootstrap.storage_account_name
}

module "vnet" {
  source = "./modules/vnet"

  location            = var.location
  project_name        = var.project_name
  environment         = var.environment
  resource_group_name = local.bootstrap_rg_name
  tags                = local.common_tags
}

module "log_analytics" {
  source = "./modules/log_analytics"

  location            = var.location
  project_name        = var.project_name
  environment         = var.environment
  resource_group_name = local.bootstrap_rg_name
  retention_days      = var.log_analytics_retention_days
  tags                = local.common_tags
}

module "key_vault" {
  source = "./modules/key_vault"

  location            = var.location
  project_name        = var.project_name
  environment         = var.environment
  resource_group_name = local.bootstrap_rg_name
  tags                = local.common_tags
  sku_name            = var.key_vault_sku
  jwt_secret          = var.jwt_secret
}

module "acr" {
  source = "./modules/acr"

  location            = var.location
  project_name        = var.project_name
  environment         = var.environment
  resource_group_name = local.bootstrap_rg_name
  tags                = local.common_tags
  sku                 = var.acr_sku
}

module "aks" {
  source = "./modules/aks"

  location                    = var.location
  project_name                = var.project_name
  environment                 = var.environment
  resource_group_name         = local.bootstrap_rg_name
  tags                        = local.common_tags
  kubernetes_version          = "1.36.2"
  node_pool_vm_size           = var.node_pool_vm_size
  node_pool_min_count         = var.node_pool_min_count
  node_pool_max_count         = var.node_pool_max_count
  node_pool_os_disk_size_gb   = var.node_pool_os_disk_size_gb
  vnet_id                     = module.vnet.vnet_id
  aks_subnet_id               = module.vnet.aks_subnet_id
  acr_id                      = module.acr.acr_id
  log_analytics_workspace_id  = module.log_analytics.workspace_id
  log_analytics_workspace_key = module.log_analytics.workspace_key
  key_vault_id                = module.key_vault.key_vault_id
}

module "application_insights" {
  source = "./modules/application_insights"

  location            = var.location
  project_name        = var.project_name
  environment         = var.environment
  resource_group_name = local.bootstrap_rg_name
  tags                = local.common_tags
  workspace_id        = module.log_analytics.workspace_resource_id
}
