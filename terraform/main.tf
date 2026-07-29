terraform {
  required_version = ">= 1.5"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

provider "aws" {
  region = var.aws_region
}

locals {
  name_prefix = "${var.project_name}-${var.environment}"
}

module "vpc" {
  source = "./modules/vpc"

  name_prefix       = local.name_prefix
  vpc_cidr          = var.vpc_cidr
  availability_zones = var.availability_zones
  public_subnets    = var.public_subnets
  private_subnets   = var.private_subnets
  environment       = var.environment
}

module "iam" {
  source = "./modules/iam"

  name_prefix        = local.name_prefix
  environment        = var.environment
}

module "sns" {
  source = "./modules/sns"

  name_prefix = local.name_prefix
  environment = var.environment
}

module "dynamodb" {
  source = "./modules/dynamodb"

  name_prefix = local.name_prefix
  environment = var.environment
  billing_mode = "PAY_PER_REQUEST"
}

module "aurora" {
  source = "./modules/aurora"

  name_prefix          = local.name_prefix
  environment          = var.environment
  vpc_id               = module.vpc.vpc_id
  private_subnet_ids   = module.vpc.private_subnet_ids
  db_username          = var.db_username
  db_password          = var.db_password
}

module "ecs_fargate" {
  source = "./modules/ecs_fargate"

  name_prefix        = local.name_prefix
  environment        = var.environment
  vpc_id             = module.vpc.vpc_id
  public_subnet_ids  = module.vpc.public_subnet_ids
  private_subnet_ids = module.vpc.private_subnet_ids
  alb_security_group_id = module.vpc.alb_security_group_id
  ecs_security_group_id = module.vpc.ecs_security_group_id
  task_execution_role_arn = module.iam.ecs_task_execution_role_arn
  task_role_arn      = module.iam.ecs_task_role_arn
  container_image    = var.container_image
  container_port     = 8080
  desired_count      = var.desired_count
  cpu                = 256
  memory             = 512
  sns_topic_arn      = module.sns.alert_topic_arn
}

module "sagemaker" {
  source = "./modules/sagemaker"

  name_prefix     = local.name_prefix
  environment     = var.environment
  role_arn        = module.iam.sagemaker_role_arn
  instance_type   = "ml.t2.medium"
  s3_bucket       = var.sagemaker_s3_bucket
}
