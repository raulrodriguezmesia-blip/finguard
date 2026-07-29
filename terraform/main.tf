module "sagemaker" {
  count           = var.environment == "prod" ? 1 : 0
  source = "./modules/sagemaker"

  name_prefix     = local.name_prefix
  environment     = var.environment
  role_arn        = module.iam.sagemaker_role_arn
  instance_type   = "ml.t2.medium"
  s3_bucket       = var.sagemaker_s3_bucket
}
