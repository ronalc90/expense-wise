# ============================================
# ExpenseWise - Variables de Terraform
# ============================================

# ── General ──────────────────────────────────
variable "project_name" {
  description = "Nombre del proyecto"
  type        = string
  default     = "expensewise"
}

variable "environment" {
  description = "Ambiente de despliegue (staging, production)"
  type        = string
  default     = "production"

  validation {
    condition     = contains(["staging", "production"], var.environment)
    error_message = "El ambiente debe ser 'staging' o 'production'."
  }
}

variable "aws_region" {
  description = "Region de AWS"
  type        = string
  default     = "us-east-1"
}

# ── Red ──────────────────────────────────────
variable "vpc_cidr" {
  description = "CIDR block para la VPC"
  type        = string
  default     = "10.0.0.0/16"
}

# ── ECS ──────────────────────────────────────
variable "container_image" {
  description = "Imagen de Docker para la aplicacion"
  type        = string
  default     = "ghcr.io/ronalc90/expensewise:latest"
}

variable "task_cpu" {
  description = "CPU para la tarea ECS (unidades: 256 = 0.25 vCPU)"
  type        = string
  default     = "512"
}

variable "task_memory" {
  description = "Memoria para la tarea ECS (en MB)"
  type        = string
  default     = "1024"
}

variable "desired_count" {
  description = "Numero deseado de tareas ECS"
  type        = number
  default     = 2
}

variable "min_capacity" {
  description = "Minimo de tareas para auto-scaling"
  type        = number
  default     = 1
}

variable "max_capacity" {
  description = "Maximo de tareas para auto-scaling"
  type        = number
  default     = 6
}

# ── Base de Datos ────────────────────────────
variable "db_instance_class" {
  description = "Tipo de instancia RDS"
  type        = string
  default     = "db.t3.micro"
}

variable "db_name" {
  description = "Nombre de la base de datos"
  type        = string
  default     = "expensewise"
}

variable "db_username" {
  description = "Usuario de la base de datos"
  type        = string
  default     = "expensewise"
}

variable "db_password" {
  description = "Contrasena de la base de datos"
  type        = string
  sensitive   = true
}

# ── Redis ────────────────────────────────────
variable "redis_node_type" {
  description = "Tipo de nodo ElastiCache"
  type        = string
  default     = "cache.t3.micro"
}

# ── Seguridad ────────────────────────────────
variable "jwt_secret" {
  description = "Secreto para firmar tokens JWT"
  type        = string
  sensitive   = true
}
