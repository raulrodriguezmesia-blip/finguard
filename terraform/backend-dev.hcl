bucket         = "finguard-terraform-state"
key            = "dev/terraform.tfstate"
region         = "us-east-1"
dynamodb_table = "finguard-terraform-locks"
encrypt        = true
