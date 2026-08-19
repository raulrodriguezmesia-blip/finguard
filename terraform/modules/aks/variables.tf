variable "location" {
  type = string
}

variable "project_name" {
  type = string
}

variable "environment" {
  type = string
}

variable "resource_group_name" {
  type = string
}

variable "tags" {
  type = map(string)
}

variable "kubernetes_version" {
  type    = string
  default = "1.30"
}

variable "node_pool_vm_size" {
  type = string
}

variable "node_pool_min_count" {
  type = number
}

variable "node_pool_max_count" {
  type = number
}

variable "node_pool_os_disk_size_gb" {
  type = number
}

variable "vnet_id" {
  type = string
}

variable "aks_subnet_id" {
  type = string
}

variable "acr_id" {
  type = string
}

variable "log_analytics_workspace_id" {
  type = string
}

variable "log_analytics_workspace_key" {
  type      = string
  sensitive = true
}

variable "key_vault_id" {
  type = string
}

variable "agw_id" {
  type    = string
  default = null
}

locals {
  name_prefix = "${var.project_name}-${var.environment}"
}
