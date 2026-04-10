# Guia DevOps - ExpenseWise

## Arquitectura General

```
                          +------------------+
                          |   GitHub Actions  |
                          |   CI/CD Pipeline  |
                          +--------+---------+
                                   |
                          +--------v---------+
                          |  GitHub Container |
                          |    Registry       |
                          |  (ghcr.io)        |
                          +--------+---------+
                                   |
                    +--------------+--------------+
                    |                              |
           +--------v---------+          +--------v---------+
           |   AWS ECS        |          |   Kubernetes     |
           |   Fargate        |          |   Cluster        |
           +--------+---------+          +--------+---------+
                    |                              |
         +----------+----------+        +----------+----------+
         |          |          |        |          |          |
    +----v---+ +---v----+ +---v---+ +--v---+ +---v----+ +---v---+
    |  ALB   | |  RDS   | | Redis | | Ingress| |  Pod  | |  HPA  |
    |        | | PgSQL  | |       | | (TLS) | |       | |       |
    +--------+ +--------+ +-------+ +------+ +-------+ +-------+
                    |
         +----------+----------+
         |                     |
    +----v-------+    +--------v-------+
    | Prometheus |    |    Grafana     |
    | (metricas) |    |  (dashboards)  |
    +------------+    +-------+--------+
                              |
                     +--------v--------+
                     |  Loki + Promtail|
                     |  (logs)         |
                     +-----------------+
```

## Pipeline CI/CD

### Etapas del pipeline (`ci.yml`)

| Etapa | Descripcion | Trigger |
|-------|-------------|---------|
| **Lint** | Compilacion + SpotBugs | Push y PR |
| **Test** | Tests con H2 en memoria | Push y PR |
| **Build** | Generar JAR ejecutable | Push y PR |
| **Docker Build** | Build + push a GHCR | Solo push a main |
| **OWASP Check** | Escaneo de dependencias | Solo push a main |
| **Deploy** | Deploy a produccion | Solo push a main |

### Pipeline de seguridad (`security.yml`)

| Escaneo | Herramienta | Frecuencia |
|---------|-------------|------------|
| Dependencias | OWASP Dependency Check | Push + semanal |
| Contenedor | Trivy | Push + semanal |
| Codigo | CodeQL | Push + semanal |
| Secretos | Gitleaks | Push + semanal |

### Secrets requeridos en GitHub

| Secret | Descripcion |
|--------|-------------|
| `GITHUB_TOKEN` | Automatico - para GHCR |
| `RAILWAY_TOKEN` | Token de Railway (si aplica) |
| `AWS_ACCESS_KEY_ID` | Credenciales AWS (si aplica) |
| `AWS_SECRET_ACCESS_KEY` | Credenciales AWS (si aplica) |

## Docker

### Construir imagen local

```bash
# Build de la imagen
docker build -t expensewise:latest .

# Ejecutar solo la app (requiere DB externa)
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=dev \
  expensewise:latest
```

### Docker Compose - Desarrollo

```bash
# Levantar PostgreSQL + Redis
docker compose up -d postgres redis

# Levantar todo el stack
docker compose up -d

# Ver logs
docker compose logs -f app

# Detener todo
docker compose down

# Detener y eliminar volumenes
docker compose down -v
```

### Docker Compose - Con monitoreo

```bash
# Stack completo con Prometheus + Grafana + Loki
docker compose -f docker-compose.yml -f docker-compose.monitoring.yml up -d

# Acceder a los servicios:
#   App:        http://localhost:8080
#   Grafana:    http://localhost:3000 (admin/admin)
#   Prometheus: http://localhost:9090
```

### Imagen optimizada

El Dockerfile usa multi-stage build:
1. **Build stage**: JDK 21 + Maven - compila el JAR
2. **Runtime stage**: JRE 21 Alpine - imagen minima (~180MB)

Caracteristicas:
- Usuario no-root (`app`)
- Health check integrado
- Soporte para contenedores (`UseContainerSupport`)
- Limite de RAM al 75% del contenedor

## Kubernetes

### Despliegue inicial

```bash
# Crear namespace
kubectl apply -f infra/kubernetes/namespace.yml

# Crear secretos (editar valores primero)
kubectl apply -f infra/kubernetes/secret.yml

# Aplicar toda la configuracion
kubectl apply -f infra/kubernetes/

# Verificar estado
kubectl get all -n expensewise
```

### Manifiestos incluidos

| Archivo | Recurso | Descripcion |
|---------|---------|-------------|
| `namespace.yml` | Namespace | Aislamiento logico |
| `configmap.yml` | ConfigMap | Variables de entorno |
| `secret.yml` | Secret | Credenciales (template) |
| `deployment.yml` | Deployment | 2 replicas, rolling update |
| `service.yml` | Service (ClusterIP) | Acceso interno |
| `ingress.yml` | Ingress (NGINX + TLS) | Acceso externo |
| `hpa.yml` | HPA | Auto-scaling CPU/memoria |

### Estrategia de deploy

- **Rolling Update**: maxSurge=1, maxUnavailable=0
- **Readiness probe**: `/actuator/health/readiness` (cada 10s)
- **Liveness probe**: `/actuator/health/liveness` (cada 15s)
- **Startup probe**: `/actuator/health` (hasta 150s para arrancar)
- **Graceful shutdown**: preStop hook con 10s de espera

### Auto-scaling

- **Min replicas**: 2
- **Max replicas**: 8
- **CPU target**: 70% utilizacion
- **Memory target**: 80% utilizacion
- **Scale up**: maximo 2 pods cada 60s
- **Scale down**: maximo 1 pod cada 120s (ventana de 5 min)

## Terraform (AWS)

### Recursos provisionados

| Recurso | Tipo | Descripcion |
|---------|------|-------------|
| VPC | Networking | 2 subnets publicas + 2 privadas |
| ALB | Load Balancer | Balanceador de carga publico |
| ECS Cluster | Compute | Cluster Fargate |
| ECS Service | Compute | 2 tareas (auto-scaling 1-6) |
| RDS PostgreSQL | Database | PostgreSQL 16, Multi-AZ en prod |
| ElastiCache | Cache | Redis 7.1 |
| CloudWatch | Logs | Retencion 30 dias |
| SSM | Secrets | Parametros seguros |

### Uso

```bash
cd infra/terraform

# Copiar y editar variables
cp terraform.tfvars.example terraform.tfvars
# Editar terraform.tfvars con valores reales

# Inicializar
terraform init

# Planificar cambios
terraform plan

# Aplicar infraestructura
terraform apply

# Ver outputs
terraform output
```

## Monitoreo

### Prometheus

Configuracion en `monitoring/prometheus.yml`:
- Scrape interval: 15s
- Endpoint: `/actuator/prometheus`
- Retencion: 30 dias

### Grafana

Dashboard pre-configurado con paneles para:
- Tasa de requests y errores
- Latencia (p50, p95, p99)
- Memoria JVM y GC
- Pool de conexiones DB
- Metricas de negocio

### Logs con Loki

```bash
# Ver logs en Grafana > Explore > Loki
# Query de ejemplo:
{job="docker"} |= "expensewise"
```

Ver `docs/OBSERVABILITY.md` para mas detalles.

## Seguridad

### Escaneos automaticos

1. **OWASP Dependency Check**: Vulnerabilidades en dependencias Maven
2. **Trivy**: Vulnerabilidades en imagen Docker y filesystem
3. **CodeQL**: Analisis estatico de seguridad del codigo Java
4. **Gitleaks**: Deteccion de secretos en el repositorio

### Buenas practicas aplicadas

- Imagen Docker con usuario no-root
- Secretos en SSM/Secrets de Kubernetes (nunca en codigo)
- Security groups restrictivos en AWS
- TLS en Ingress de Kubernetes
- Rate limiting en NGINX Ingress
- Dependencias escaneadas semanalmente

## Estrategia de escalamiento

### Horizontal

| Componente | Min | Max | Metrica |
|------------|-----|-----|---------|
| ECS Tasks | 1 | 6 | CPU > 70% |
| K8s Pods | 2 | 8 | CPU > 70%, Mem > 80% |

### Vertical

| Componente | Desarrollo | Produccion |
|------------|-----------|------------|
| App | 512MB RAM | 1GB RAM |
| PostgreSQL | db.t3.micro | db.t3.medium |
| Redis | cache.t3.micro | cache.t3.small |

## Recuperacion ante desastres

### Backups

- **RDS**: Backups automaticos con retencion de 7 dias
- **Ventana de backup**: 03:00-04:00 UTC
- **Snapshot final**: Habilitado en produccion

### Rollback

```bash
# ECS - rollback automatico con circuit breaker
# K8s - rollback manual:
kubectl rollout undo deployment/expensewise -n expensewise

# Ver historial de deployments:
kubectl rollout history deployment/expensewise -n expensewise
```

### RTO/RPO estimados

| Metrica | Objetivo |
|---------|----------|
| **RTO** (tiempo de recuperacion) | < 15 minutos |
| **RPO** (punto de recuperacion) | < 1 hora |
