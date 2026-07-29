variable "db_instance_class" {
  type    = string
  default = "db.t3.medium"
}

variable "db_instance_count" {
  type    = number
  default = 1
}

variable "skip_final_snapshot" {
  type    = bool
  default = true
}

variable "environment" {
  type    = string
  default = "prod"
}

resource "aws_db_subnet_group" "aurora" {
  name       = "${var.name_prefix}-aurora-subnet-group"
  subnet_ids = var.private_subnet_ids

  tags = {
    Name        = "${var.name_prefix}-aurora-subnet-group"
    Environment = var.environment
  }
}

resource "aws_security_group" "aurora" {
  name        = "${var.name_prefix}-aurora-sg"
  description = "Security group for Aurora PostgreSQL"
  vpc_id      = var.vpc_id

  ingress {
    from_port       = 5432
    to_port         = 5432
    protocol        = "tcp"
    security_groups = [] # Permitir desde ECS (se puede refinar)
    cidr_blocks     = ["10.0.0.0/16"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name        = "${var.name_prefix}-aurora-sg"
    Environment = var.environment
  }
}

resource "aws_rds_cluster" "aurora" {
  cluster_identifier      = "${var.name_prefix}-aurora-cluster"
  engine                  = "aurora-postgresql"
  engine_version          = "15.5"
  database_name           = "finguard"
  master_username         = var.db_username
  master_password         = var.db_password
  db_subnet_group_name    = aws_db_subnet_group.aurora.name
  vpc_security_group_ids  = [aws_security_group.aurora.id]
  skip_final_snapshot     = var.skip_final_snapshot
  backup_retention_period = var.environment == "prod" ? 7 : 1

  tags = {
    Name        = "${var.name_prefix}-aurora-cluster"
    Environment = var.environment
  }
}

resource "aws_rds_cluster_instance" "aurora_instances" {
  count              = var.db_instance_count
  identifier         = "${var.name_prefix}-aurora-instance-${count.index + 1}"
  cluster_identifier = aws_rds_cluster.aurora.id
  engine             = aws_rds_cluster.aurora.engine
  instance_class     = var.db_instance_class
  db_subnet_group_name = aws_db_subnet_group.aurora.name

  tags = {
    Name        = "${var.name_prefix}-aurora-instance-${count.index + 1}"
    Environment = var.environment
  }
}

output "aurora_endpoint" {
  value = aws_rds_cluster.aurora.endpoint
}

output "aurora_reader_endpoint" {
  value = aws_rds_cluster.aurora.reader_endpoint
}
