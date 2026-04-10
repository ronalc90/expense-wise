# Observabilidad - ExpenseWise

## Dependencias requeridas en `pom.xml`

Para habilitar metricas de Prometheus y health checks avanzados, agregar las siguientes dependencias:

```xml
<!-- Actuator - health checks y endpoints de gestion -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>

<!-- Micrometer - Prometheus registry -->
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

## Configuracion en `application.yml`

Agregar al archivo `application.yml` de produccion:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,prometheus,metrics
  endpoint:
    health:
      show-details: always
      probes:
        enabled: true
  metrics:
    tags:
      application: expensewise
    distribution:
      percentiles-histogram:
        http.server.requests: true
      sla:
        http.server.requests: 50ms,100ms,200ms,500ms,1s
```

## Endpoints disponibles

| Endpoint | Descripcion |
|----------|-------------|
| `/actuator/health` | Estado general de la aplicacion |
| `/actuator/health/readiness` | Kubernetes readiness probe |
| `/actuator/health/liveness` | Kubernetes liveness probe |
| `/actuator/prometheus` | Metricas en formato Prometheus |
| `/actuator/metrics` | Metricas en formato JSON |
| `/actuator/info` | Informacion de la aplicacion |

## Metricas clave

### HTTP
- `http_server_requests_seconds_count` - Total de requests
- `http_server_requests_seconds_sum` - Tiempo total de respuesta
- `http_server_requests_seconds_bucket` - Histograma de latencia

### JVM
- `jvm_memory_used_bytes` - Memoria usada por area (heap/non-heap)
- `jvm_memory_max_bytes` - Memoria maxima disponible
- `jvm_gc_pause_seconds` - Pausas del garbage collector
- `jvm_threads_live_threads` - Threads activos

### Base de datos (HikariCP)
- `hikaricp_connections_active` - Conexiones activas
- `hikaricp_connections_idle` - Conexiones inactivas
- `hikaricp_connections_pending` - Conexiones pendientes
- `hikaricp_connections_acquire_seconds` - Tiempo de adquisicion

## Stack de observabilidad

### Componentes
1. **Prometheus** (puerto 9090) - Recoleccion y almacenamiento de metricas
2. **Grafana** (puerto 3000) - Visualizacion y dashboards
3. **Loki** (puerto 3100) - Agregacion de logs
4. **Promtail** - Recolector de logs de contenedores

### Levantar el stack completo

```bash
# Stack completo (app + infra + monitoreo)
docker compose -f docker-compose.yml -f docker-compose.monitoring.yml up -d
```

### Acceso a dashboards

- **Grafana**: http://localhost:3000 (admin/admin)
- **Prometheus**: http://localhost:9090

### Dashboard pre-configurado

El dashboard `grafana/dashboards/expensewise.json` incluye paneles para:
- Tasa de requests (req/s)
- Tasa de errores (5xx)
- Tiempo de respuesta (p50, p95, p99)
- Memoria JVM (Heap)
- Pausas del GC
- Threads activos
- Pool de conexiones HikariCP
- Gastos creados por hora
- Usuarios activos (logins)

## Metricas de negocio personalizadas

Para agregar metricas de negocio, inyectar `MeterRegistry`:

```java
@Service
public class ExpenseService {

    private final Counter expensesCreated;

    public ExpenseService(MeterRegistry meterRegistry) {
        this.expensesCreated = Counter.builder("expenses.created.total")
            .description("Total de gastos creados")
            .tag("application", "expensewise")
            .register(meterRegistry);
    }

    public ExpenseResponse createExpense(ExpenseRequest request) {
        // ... logica de negocio ...
        expensesCreated.increment();
        return response;
    }
}
```

## Alertas recomendadas

Configurar en Grafana o Prometheus AlertManager:

| Alerta | Condicion | Severidad |
|--------|-----------|-----------|
| Alta tasa de errores | error_rate > 5% por 5 min | Critica |
| Latencia elevada | p95 > 2s por 5 min | Warning |
| Memoria JVM alta | heap > 85% por 10 min | Warning |
| Pool DB saturado | active_connections > 80% por 5 min | Critica |
| Aplicacion caida | up == 0 por 1 min | Critica |
