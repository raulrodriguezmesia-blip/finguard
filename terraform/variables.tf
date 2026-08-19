variable "project_name" {
  description = "Nombre del proyecto"
  type        = string
  default     = "finguard"
}

variable "environment" {
  description = "Ambiente (dev, staging, prod)"
  type        = string
  default     = "dev"
  validation {
    condition     = contains(["dev", "staging", "prod"], var.environment)
    error_message = "El ambiente debe ser dev, staging o prod."
  }
}

variable "location" {
  description = "Región de Azure"
  type        = string
  default     = "eastus"
}

variable "tags" {
  description = "Tags globales"
  type        = map(string)
  default = {
    project     = "finguard"
    managed-by  = "terraform"
    environment = "dev"
  }
}

variable "node_pool_vm_size" {
  description = "Tamaño de VM para node pool de usuario"
  type        = string
  default     = "Standard_D2s_v3"
}

variable "node_pool_min_count" {
  description = "Cantidad mínima de nodos en el pool de usuario"
  type        = number
  default     = 1
}

variable "node_pool_max_count" {
  description = "Cantidad máxima de nodos en el pool de usuario"
  type        = number
  default     = 5
}

variable "node_pool_os_disk_size_gb" {
  description = "Tamaño de disco OS de nodos"
  type        = number
  default     = 128
}

variable "acr_sku" {
  description = "SKU de ACR"
  type        = string
  default     = "Standard"
  validation {
    condition     = contains(["Basic", "Standard", "Premium"], var.acr_sku)
    error_message = "El SKU de ACR debe ser Basic, Standard o Premium."
  }
}

variable "log_analytics_retention_days" {
  description = "Días de retención de logs en Log Analytics"
  type        = number
  default     = 30
}

variable "key_vault_sku" {
  description = "SKU de Key Vault"
  type        = string
  default     = "standard"
}

variable "jwt_secret" {
  description = "Secret para JWT (se almacena en Key Vault)"
  type        = string
  sensitive   = true
  default     = "default-secret-key-change-me-in-production"
}

variable "kafka_bootstrap_servers" {
  description = "Bootstrap servers de Kafka (para configurar app)"
  type        = string
  default     = ""
}

variable "kafka_username" {
  description = "Usuario SASL para Kafka"
  type        = string
  default     = ""
}

variable "kafka_password" {
  description = "Password SASL para Kafka"
  type        = string
  sensitive   = true
  default     = ""
}
