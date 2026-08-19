resource "azurerm_kubernetes_cluster" "main" {
  name                = "aks-${local.name_prefix}"
  location            = var.location
  resource_group_name = var.resource_group_name
  dns_prefix          = "${local.name_prefix}-dns"
  kubernetes_version  = var.kubernetes_version
  tags                = var.tags

  # Identidad administrada (sin service principal)
  identity {
    type = "SystemAssigned"
  }

  # Red
  network_profile {
    network_plugin     = "azure" # Azure CNI Overlay
    network_policy     = "azure"
    dns_service_ip     = "10.0.3.10"
    service_cidr       = "10.0.3.0/24"
    docker_bridge_cidr = "172.17.0.1/16"
  }

  # API Server: privado en prod, público en dev/staging
  dynamic "api_server_access_profile" {
    for_each = var.environment == "prod" ? [1] : []
    content {
      authorized_ip_ranges = []
      subnet_id            = var.aks_subnet_id
    }
  }

  # Default node pool (system)
  default_node_pool {
    name            = "system"
    vm_size = var.environment == "dev" ? "Standard_D2s_v7" : "Standard_D4s_v3"
    node_count      = var.environment == "dev" ? 1 : 3
    os_disk_size_gb = var.node_pool_os_disk_size_gb
    vnet_subnet_id  = var.aks_subnet_id
    zones           = var.environment == "prod" ? ["1", "2", "3"] : []

    upgrade_settings {
      max_surge = "10%"
    }
  }

  # Auto-scaling del cluster
  auto_scaler_profile {
    balance_similar_node_groups      = true
    expander                         = "least-waste"
    max_graceful_termination_sec     = 600
    scale_down_delay_after_add       = "10m"
    scale_down_unneeded              = "10m"
    scale_down_unready               = "20m"
    scale_down_utilization_threshold = 0.5
  }

  # Azure Policy / Guardrails
  azure_policy_enabled = true

  # Key Vault secrets provider (CSI driver)
  key_vault_secrets_provider {
    secret_rotation_enabled  = true
    secret_rotation_interval = "2m"
  }

  # Integración con ACR - AAD RBAC simplificado
  role_based_access_control_enabled = true

  oidc_issuer_enabled       = true
  workload_identity_enabled = true

  # Ingress controller (AGIC para prod, nginx para dev)
  dynamic "ingress_application_gateway" {
    for_each = var.environment == "prod" ? [1] : []
    content {
      gateway_id  = var.agw_id
      subnet_cidr = "10.0.4.0/24"
    }
  }

  lifecycle {
    ignore_changes = [
      default_node_pool[0].node_count,
      api_server_access_profile
    ]
  }
}

# User node pool para workloads de la app
resource "azurerm_kubernetes_cluster_node_pool" "user" {
  name                  = "user"
  kubernetes_cluster_id = azurerm_kubernetes_cluster.main.id
  vm_size               = var.node_pool_vm_size
  node_count            = var.environment == "dev" ? 1 : 2
  min_count             = var.node_pool_min_count
  max_count             = var.node_pool_max_count
  enable_auto_scaling   = true
  os_disk_size_gb       = var.node_pool_os_disk_size_gb
  vnet_subnet_id        = var.aks_subnet_id
  zones                 = var.environment == "prod" ? ["1", "2", "3"] : []

  mode = "User"

  upgrade_settings {
    max_surge = "10%"
  }

  lifecycle {
    ignore_changes = [node_count]
  }
}

# NOTE: ACR role assignment commented out due to Free Trial ABAC condition blocking
# Managed identity can pull from ACR without explicit role assignment in many cases
# If needed, assign manually after apply:
#   az role assignment create --assignee <kubelet_identity> --role AcrPull --scope <acr_resource_id>
