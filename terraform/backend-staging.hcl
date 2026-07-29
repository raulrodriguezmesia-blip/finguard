bucket         = "finguard-terraform-state"
key            = "staging/terraform.tfstate"
region         = "us-east-1"
dynamodb_table = "finguard-terraform-locks"
encrypt        = true
