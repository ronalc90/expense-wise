# ============================================
# ExpenseWise - Outputs de Terraform
# ============================================

output "alb_dns_name" {
  description = "URL del Application Load Balancer"
  value       = aws_lb.main.dns_name
}

output "alb_url" {
  description = "URL completa de la aplicacion"
  value       = "http://${aws_lb.main.dns_name}"
}

output "ecs_cluster_name" {
  description = "Nombre del cluster ECS"
  value       = aws_ecs_cluster.main.name
}

output "ecs_service_name" {
  description = "Nombre del servicio ECS"
  value       = aws_ecs_service.app.name
}

output "rds_endpoint" {
  description = "Endpoint de la base de datos RDS"
  value       = aws_db_instance.main.endpoint
}

output "rds_address" {
  description = "Direccion del host RDS"
  value       = aws_db_instance.main.address
}

output "redis_endpoint" {
  description = "Endpoint del cluster Redis"
  value       = aws_elasticache_cluster.main.cache_nodes[0].address
}

output "vpc_id" {
  description = "ID de la VPC"
  value       = aws_vpc.main.id
}

output "private_subnet_ids" {
  description = "IDs de las subnets privadas"
  value       = aws_subnet.private[*].id
}

output "public_subnet_ids" {
  description = "IDs de las subnets publicas"
  value       = aws_subnet.public[*].id
}

output "cloudwatch_log_group" {
  description = "Grupo de logs en CloudWatch"
  value       = aws_cloudwatch_log_group.app.name
}
