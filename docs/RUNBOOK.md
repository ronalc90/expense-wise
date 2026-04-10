# Runbook de Operaciones - ExpenseWise

## Indice

1. [Como desplegar](#como-desplegar)
2. [Como hacer rollback](#como-hacer-rollback)
3. [Como revisar logs](#como-revisar-logs)
4. [Como responder a alertas](#como-responder-a-alertas)
5. [Backup y restauracion de BD](#backup-y-restauracion-de-bd)
6. [Problemas comunes y soluciones](#problemas-comunes-y-soluciones)

---

## Como desplegar

### Deploy automatico (CI/CD)

El pipeline se ejecuta automaticamente al hacer push a `main`:

```bash
git push origin main
# El pipeline ejecuta: lint -> test -> build -> docker-build -> deploy
```

Verificar estado del deploy:
- GitHub Actions: `https://github.com/<owner>/expensewise/actions`

### Deploy manual con Docker Compose

```bash
cd /ruta/al/proyecto

# Descargar la ultima imagen
docker compose pull app

# Reiniciar con la nueva imagen
docker compose up -d app

# Verificar que arranco correctamente
docker compose ps
docker compose logs -f app --tail=50
```

### Deploy manual en Kubernetes

```bash
# Actualizar imagen
kubectl set image deployment/expensewise \
  expensewise=ghcr.io/ronalc90/expensewise:SHA_DEL_COMMIT \
  -n expensewise

# Monitorear el rollout
kubectl rollout status deployment/expensewise -n expensewise

# Verificar pods
kubectl get pods -n expensewise -w
```

### Deploy manual en AWS ECS

```bash
# Forzar nuevo despliegue con la ultima imagen
aws ecs update-service \
  --cluster expensewise-cluster \
  --service expensewise-service \
  --force-new-deployment

# Verificar estado
aws ecs describe-services \
  --cluster expensewise-cluster \
  --services expensewise-service \
  --query 'services[0].{status:status,running:runningCount,desired:desiredCount}'
```

---

## Como hacer rollback

### Rollback en Kubernetes

```bash
# Ver historial de revisiones
kubectl rollout history deployment/expensewise -n expensewise

# Rollback a la version anterior
kubectl rollout undo deployment/expensewise -n expensewise

# Rollback a una revision especifica
kubectl rollout undo deployment/expensewise --to-revision=3 -n expensewise

# Verificar
kubectl rollout status deployment/expensewise -n expensewise
```

### Rollback en Docker Compose

```bash
# Usar una imagen especifica anterior
docker compose down app
docker compose up -d app --pull=never

# O especificar un tag anterior en docker-compose.yml
# image: ghcr.io/ronalc90/expensewise:SHA_ANTERIOR
```

### Rollback en ECS

ECS tiene circuit breaker automatico. Si falla:

```bash
# Forzar rollback manual actualizando task definition
aws ecs update-service \
  --cluster expensewise-cluster \
  --service expensewise-service \
  --task-definition expensewise:VERSION_ANTERIOR
```

---

## Como revisar logs

### Docker Compose

```bash
# Logs de la aplicacion
docker compose logs -f app

# Ultimas 100 lineas
docker compose logs --tail=100 app

# Logs de todos los servicios
docker compose logs -f

# Filtrar por nivel
docker compose logs app 2>&1 | grep "ERROR"
```

### Kubernetes

```bash
# Logs del pod actual
kubectl logs -f deployment/expensewise -n expensewise

# Logs de un pod especifico
kubectl logs -f POD_NAME -n expensewise

# Logs anteriores (si el pod reinicio)
kubectl logs --previous POD_NAME -n expensewise

# Logs de los ultimos 30 minutos
kubectl logs --since=30m deployment/expensewise -n expensewise
```

### AWS CloudWatch

```bash
# Ver logs recientes
aws logs tail /ecs/expensewise --follow

# Filtrar por patron
aws logs filter-log-events \
  --log-group-name /ecs/expensewise \
  --filter-pattern "ERROR" \
  --start-time $(date -d '1 hour ago' +%s)000
```

### Grafana + Loki

1. Abrir Grafana: `http://localhost:3000`
2. Ir a **Explore** > seleccionar datasource **Loki**
3. Queries utiles:
   ```
   {job="docker"} |= "expensewise"
   {job="docker"} |= "ERROR"
   {job="docker"} |= "Exception"
   ```

---

## Como responder a alertas

### Alta tasa de errores (> 5%)

**Severidad**: Critica

1. Verificar health check:
   ```bash
   curl -s http://localhost:8080/actuator/health | jq .
   ```

2. Revisar logs buscando excepciones:
   ```bash
   docker compose logs --tail=200 app | grep -i "error\|exception"
   ```

3. Verificar conectividad a PostgreSQL:
   ```bash
   docker compose exec postgres pg_isready -U expensewise
   ```

4. Verificar conectividad a Redis:
   ```bash
   docker compose exec redis redis-cli ping
   ```

5. Si el problema es de la app, hacer rollback (ver seccion anterior).

### Latencia elevada (p95 > 2s)

**Severidad**: Warning

1. Verificar metricas en Grafana:
   - Panel "Tiempo de Respuesta (percentiles)"
   - Panel "Pool de Conexiones (HikariCP)"

2. Verificar queries lentas:
   ```bash
   docker compose exec postgres psql -U expensewise -c \
     "SELECT pid, now() - pg_stat_activity.query_start AS duration, query
      FROM pg_stat_activity
      WHERE state != 'idle'
      ORDER BY duration DESC
      LIMIT 10;"
   ```

3. Verificar pool de conexiones saturado:
   ```bash
   curl -s http://localhost:8080/actuator/metrics/hikaricp.connections.active | jq .
   ```

4. Si hay queries lentas, optimizar indices o la query.
5. Si el pool esta saturado, considerar aumentar `maximumPoolSize`.

### Memoria JVM alta (> 85%)

**Severidad**: Warning

1. Verificar metricas de memoria:
   ```bash
   curl -s http://localhost:8080/actuator/metrics/jvm.memory.used | jq .
   ```

2. Verificar pausas de GC:
   ```bash
   curl -s http://localhost:8080/actuator/metrics/jvm.gc.pause | jq .
   ```

3. Si la memoria sigue subiendo, posible memory leak. Generar heap dump:
   ```bash
   # En Kubernetes
   kubectl exec -it POD_NAME -n expensewise -- \
     jcmd 1 GC.heap_dump /tmp/heapdump.hprof

   kubectl cp expensewise/POD_NAME:/tmp/heapdump.hprof ./heapdump.hprof
   ```

4. Analizar el heap dump con Eclipse MAT o VisualVM.

### Aplicacion caida

**Severidad**: Critica

1. Verificar estado de pods/contenedores:
   ```bash
   # Docker
   docker compose ps

   # Kubernetes
   kubectl get pods -n expensewise
   kubectl describe pod POD_NAME -n expensewise
   ```

2. Si hay CrashLoopBackOff, revisar logs del pod anterior:
   ```bash
   kubectl logs --previous POD_NAME -n expensewise
   ```

3. Causas comunes:
   - Base de datos inaccesible
   - Variable de entorno faltante
   - Puerto en uso
   - OOMKilled (memoria insuficiente)

4. Remediar segun la causa y hacer rollback si es necesario.

---

## Backup y restauracion de BD

### Backup manual (PostgreSQL)

```bash
# Usando Docker Compose
docker compose exec postgres pg_dump -U expensewise expensewise > backup_$(date +%Y%m%d_%H%M%S).sql

# Backup comprimido
docker compose exec postgres pg_dump -U expensewise -Fc expensewise > backup_$(date +%Y%m%d_%H%M%S).dump
```

### Restaurar backup

```bash
# Desde SQL plano
docker compose exec -T postgres psql -U expensewise expensewise < backup_20260410_120000.sql

# Desde formato custom (.dump)
docker compose exec -T postgres pg_restore -U expensewise -d expensewise --clean backup_20260410_120000.dump
```

### Backups en AWS RDS

```bash
# Crear snapshot manual
aws rds create-db-snapshot \
  --db-instance-identifier expensewise-db \
  --db-snapshot-identifier expensewise-manual-$(date +%Y%m%d)

# Listar snapshots
aws rds describe-db-snapshots \
  --db-instance-identifier expensewise-db \
  --query 'DBSnapshots[*].{ID:DBSnapshotIdentifier,Status:Status,Created:SnapshotCreateTime}'

# Restaurar desde snapshot (crea nueva instancia)
aws rds restore-db-instance-from-db-snapshot \
  --db-instance-identifier expensewise-db-restored \
  --db-snapshot-identifier expensewise-manual-20260410
```

---

## Problemas comunes y soluciones

### La aplicacion no arranca

**Sintoma**: Container reinicia continuamente.

```bash
# Revisar logs
docker compose logs --tail=50 app
```

| Causa probable | Solucion |
|---------------|----------|
| `Connection refused` a PostgreSQL | Verificar que postgres esta healthy: `docker compose ps` |
| `Flyway migration failed` | Revisar migraciones SQL en `src/main/resources/db/migration/` |
| `JWT_SECRET` no configurado | Establecer variable de entorno |
| `Port already in use` | `lsof -i :8080` y detener el proceso |

### No se puede conectar a la base de datos

```bash
# Verificar que PostgreSQL esta corriendo
docker compose exec postgres pg_isready -U expensewise

# Verificar conectividad desde la app
docker compose exec app wget -qO- http://localhost:8080/actuator/health

# Verificar credenciales
docker compose exec postgres psql -U expensewise -c "SELECT 1;"
```

### Memoria insuficiente (OOMKilled)

```bash
# En Kubernetes
kubectl describe pod POD_NAME -n expensewise | grep -A5 "Last State"

# Solucion: aumentar limites de memoria en deployment.yml
# resources.limits.memory: 1Gi -> 2Gi
```

### Contenedor Docker muy grande

```bash
# Verificar tamano de la imagen
docker images expensewise

# Si es > 300MB, verificar que el Dockerfile usa multi-stage
# y que no copia archivos innecesarios
```

### Pipeline de CI/CD falla

| Error | Solucion |
|-------|----------|
| Tests fallan | Revisar `target/surefire-reports/` en artifacts |
| Docker build falla | Verificar que `mvnw` tiene permisos de ejecucion |
| Push a GHCR falla | Verificar `packages: write` en permisos |
| OWASP timeout | Agregar `-DtimeoutSeconds=600` |

### Redis no disponible

```bash
# Verificar estado
docker compose exec redis redis-cli ping

# Verificar memoria usada
docker compose exec redis redis-cli info memory

# Flush cache si es necesario (NO en produccion sin aprobacion)
# docker compose exec redis redis-cli FLUSHALL
```

---

## Contactos de escalamiento

| Nivel | Responsable | Tiempo respuesta |
|-------|-------------|-----------------|
| L1 | Equipo de desarrollo | < 15 min |
| L2 | DevOps / SRE | < 30 min |
| L3 | Arquitectura | < 1 hora |

## Ventanas de mantenimiento

| Actividad | Ventana | Frecuencia |
|-----------|---------|------------|
| Actualizaciones de seguridad | Lunes 04:00-05:00 UTC | Semanal |
| Backups manuales | Domingo 03:00-04:00 UTC | Semanal |
| Analisis de rendimiento | Primer lunes del mes | Mensual |
