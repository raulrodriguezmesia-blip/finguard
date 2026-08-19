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

variable "sku_name" {
  type    = string
  default = "standard"
}

variable "jwt_secret" {
  type      = string
  sensitive = true
}

locals {
  name_prefix = "${var.project_name}-${var.environment}"
}
