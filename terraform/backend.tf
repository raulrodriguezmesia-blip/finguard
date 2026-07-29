terraform {
  backend "s3" {
    bucket         = "finguard-terraform-state"
    key            = "prod/terraform.tfstate"
    region         = "us-east-1"
    dynamodb_table = "finguard-terraform-locks"
    encrypt        = true
  }
}
