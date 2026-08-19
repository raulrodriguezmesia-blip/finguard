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

variable "workspace_id" {
  description = "Log Analytics workspace ID for App Insights (optional, set to null to use default)"
  type        = string
  default     = null
}

locals {
  name_prefix = "${var.project_name}-${var.environment}"
}
