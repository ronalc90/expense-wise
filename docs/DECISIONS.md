# Architecture Decision Records (ADR)

Este documento contiene las decisiones de arquitectura del proyecto ExpenseWise en formato ADR.

---

## ADR-001: Uso de Spring Boot 3 sobre alternativas

**Estado:** Aceptado  
**Fecha:** 2026-01-15  
**Contexto:**

Se necesitaba un framework backend para construir una API REST con autenticacion, acceso a datos y generacion de reportes. Las alternativas evaluadas fueron:

- **Spring Boot 3.2** - Framework maduro del ecosistema Java
- **Quarkus 3.x** - Framework nativo para cloud de Red Hat
- **Micronaut 4.x** - Framework con compilacion ahead-of-time

**Decision:**

Se elige **Spring Boot 3.2.5** como framework backend.

**Razones:**

1. **Ecosistema maduro**: Spring Boot tiene la documentacion mas extensa, la comunidad mas grande y el mayor numero de integraciones probadas en produccion.
2. **Auto-configuracion**: Reduce drasticamente el boilerplate para configurar Spring Security, Spring Data JPA, validacion y Swagger.
3. **Compatibilidad Java 21**: Soporte completo para records, pattern matching, sealed classes y virtual threads.
4. **Spring Security**: Integracion nativa con JWT, BCrypt y cadenas de filtros configurables.
5. **Spring Data JPA**: Queries derivadas y paginacion lista para usar.
6. **Soporte empresarial**: Spring tiene soporte comercial de VMware/Broadcom, garantizando mantenimiento a largo plazo.

**Consecuencias:**

- El tiempo de arranque es mayor que Quarkus/Micronaut (aceptable para este caso de uso).
- Mayor consumo de memoria base comparado con frameworks nativos (no es critico para un MVP).
- Amplio pool de desarrolladores Java familiarizados con Spring.

---

## ADR-002: JWT para autenticacion

**Estado:** Aceptado  
**Fecha:** 2026-01-15  
**Contexto:**

Se necesitaba un mecanismo de autenticacion para la API REST. Las alternativas evaluadas fueron:

- **Sesiones HTTP** con cookies
- **JWT (JSON Web Tokens)**
- **OAuth 2.0** con servidor de autorizacion externo
- **API Keys**

**Decision:**

Se elige **JWT con HMAC-SHA256** usando la biblioteca jjwt 0.12.5.

**Razones:**

1. **Stateless**: No se necesita almacenar sesiones en el servidor ni en Redis. Cada request es independiente.
2. **Escalabilidad horizontal**: Al no depender de sesiones, se pueden agregar multiples instancias del servidor sin sincronizacion.
3. **Estandar abierto**: JWT es un RFC 7519, con soporte en practicamente todos los lenguajes y frameworks.
4. **Clock inyectable**: Se inyecta un `java.time.Clock` en `JwtTokenProvider`, permitiendo testing determinista de escenarios de expiracion.
5. **Simplicidad**: Para un MVP, un sistema JWT propio es suficiente sin la complejidad de un servidor OAuth.

**Implementacion:**

- `JwtTokenProvider`: genera y valida tokens, inyecta `Clock` y `JwtProperties`
- `JwtAuthenticationFilter`: extrae el token del header `Authorization: Bearer <token>`
- `JwtProperties`: record inmutable con `@ConfigurationProperties`
- Token expira en 24 horas (configurable via `app.jwt.expiration-ms`)

**Consecuencias:**

- Los tokens no se pueden revocar individualmente (se necesitaria una blacklist en Redis para logout forzado).
- El secreto JWT debe ser de al menos 256 bits para HS256.
- Para un futuro con multiples microservicios, se deberia migrar a OAuth 2.0 con un servidor de autorizacion.

---

## ADR-003: Records de Java para DTOs

**Estado:** Aceptado  
**Fecha:** 2026-01-15  
**Contexto:**

Se necesitaban objetos para transferir datos entre la capa de presentacion y la capa de negocio, separados de las entidades JPA.

Las alternativas eran:

- **Clases POJO** con getters/setters
- **Clases con Lombok** (@Data, @Value)
- **Java Records** (desde Java 14)

**Decision:**

Se usan **Java Records** para todos los DTOs de request y response.

**Razones:**

1. **Inmutabilidad garantizada**: Los records son inmutables por definicion del lenguaje. No se puede alterar un DTO despues de su creacion.
2. **Reduccion de boilerplate**: Un record genera automaticamente constructor, getters, `equals()`, `hashCode()` y `toString()`.
3. **Bean Validation**: Los records soportan anotaciones de validacion (`@NotBlank`, `@Email`, `@DecimalMin`) directamente en los parametros del constructor.
4. **Jackson**: Los records se serializan/deserializan automaticamente con Jackson sin configuracion adicional.
5. **Separacion SRP**: Al usar records para DTOs y entidades JPA (@Entity con Lombok) por separado, cada clase tiene una sola responsabilidad.

**Ejemplos en el proyecto:**

```java
// Request DTO con validaciones
public record RegisterRequest(
    @NotBlank String name,
    @Email String email,
    @Size(min = 8) String password,
    String currency
) {}

// Response DTO inmutable
public record AuthResponse(
    String token,
    String email,
    String name
) {}
```

**Consecuencias:**

- No se puede usar Lombok `@Builder` en records (se usa el constructor canonico).
- La conversion entre entidades y records se hace manualmente en los services (podria usarse MapStruct si crece la complejidad).

---

## ADR-004: H2 para desarrollo, PostgreSQL para produccion

**Estado:** Aceptado  
**Fecha:** 2026-01-15  
**Contexto:**

Se necesitaba una estrategia de base de datos que permitiera desarrollo rapido sin dependencias externas, pero que usara una base de datos robusta en produccion.

**Decision:**

Se usa una estrategia de **dual database**:

- **Desarrollo/Testing:** H2 in-memory con `MODE=PostgreSQL`
- **Produccion:** PostgreSQL 16

**Razones:**

1. **Cero configuracion en desarrollo**: No se necesita Docker ni PostgreSQL instalado para empezar a desarrollar.
2. **Tests rapidos**: H2 in-memory se crea y destruye en milisegundos, permitiendo tests de integracion rapidos.
3. **Compatibilidad SQL**: H2 en modo PostgreSQL (`MODE=PostgreSQL`) soporta la mayoria de la sintaxis SQL de PostgreSQL.
4. **Flyway selectivo**: Las migraciones Flyway solo se activan en produccion. En desarrollo, Hibernate genera el esquema con `create-drop`.
5. **Datos seed separados**: `data.sql` se ejecuta solo en el perfil `dev` para insertar categorias predeterminadas.

**Configuracion por perfil:**

| Aspecto | `dev` | `test` | `prod` |
|---------|-------|--------|--------|
| Base de datos | H2 mem | H2 mem | PostgreSQL 16 |
| Flyway | Desactivado | Desactivado | Activado |
| DDL auto | create-drop | create-drop | validate |
| Puerto | 3001 | - | 8080 |
| SQL seed | data.sql | - | V4 migration |

**Consecuencias:**

- Algunas funciones avanzadas de PostgreSQL (CTEs recursivos, JSON operators) no estarian disponibles en H2.
- Se debe validar que las migraciones Flyway sean compatibles con la sintaxis que H2 soporta en modo PostgreSQL.
- El indice compuesto y las funciones de agregacion usadas en el proyecto son compatibles con ambos motores.

---

## ADR-005: Flyway para migraciones

**Estado:** Aceptado  
**Fecha:** 2026-01-15  
**Contexto:**

Se necesitaba un sistema para gestionar la evolucion del esquema de base de datos de forma controlada y reproducible.

Las alternativas eran:

- **Hibernate auto-DDL** (ddl-auto: update)
- **Flyway** con scripts SQL
- **Liquibase** con XML/YAML

**Decision:**

Se elige **Flyway** con scripts SQL nativos.

**Razones:**

1. **Scripts SQL versionados**: Cada migracion es un archivo SQL con nomenclatura `V{n}__{descripcion}.sql`, rastreable en Git.
2. **Control total**: Se escribe SQL nativo, sin depender de la generacion automatica de Hibernate que puede producir DDL inesperado.
3. **Validacion**: En produccion, `ddl-auto: validate` verifica que las entidades JPA coincidan con el esquema, detectando discrepancias tempranamente.
4. **Datos como migracion**: La insercion de categorias predeterminadas es una migracion (`V4__seed_default_categories.sql`), asegurando que se ejecute exactamente una vez.
5. **Simplicidad**: Flyway con SQL es mas simple que Liquibase con XML, suficiente para las necesidades actuales.

**Migraciones actuales:**

```
V1__create_users.sql          - Tabla users + indice email
V2__create_categories.sql     - Tabla categories + constraint unique
V3__create_expenses.sql       - Tabla expenses + 4 indices
V4__seed_default_categories.sql - 14 categorias predeterminadas
```

**Consecuencias:**

- Los scripts SQL deben ser compatibles con PostgreSQL (el dialecto objetivo en produccion).
- Las migraciones son inmutables: una vez aplicadas, no se deben modificar. Los cambios requieren nuevas migraciones.
- En desarrollo, Flyway esta desactivado y H2 usa `create-drop`, lo que simplifica la iteracion.

---

## ADR-006: Patron Repository con Spring Data JPA

**Estado:** Aceptado  
**Fecha:** 2026-01-15  
**Contexto:**

Se necesitaba un patron de acceso a datos que equilibrara productividad, rendimiento y control sobre las queries.

Las alternativas eran:

- **JDBC directo** con JdbcTemplate
- **Spring Data JPA** con queries derivadas + JPQL
- **MyBatis** con mapeo XML
- **jOOQ** con queries type-safe

**Decision:**

Se elige **Spring Data JPA** con queries JPQL personalizadas.

**Razones:**

1. **Queries derivadas**: Para operaciones simples (`findByEmail`, `existsByEmail`), Spring Data genera la query automaticamente.
2. **JPQL personalizado**: Para consultas complejas como filtros combinados y agregaciones del dashboard, se usa `@Query` con JPQL.
3. **JOIN FETCH**: Se usa `JOIN FETCH e.category` explicitamente en queries que cargan gastos, evitando el problema N+1.
4. **Paginacion nativa**: `Page<Expense>` y `Pageable` se integran directamente con los endpoints REST.
5. **Tipo-seguro en compilacion**: A diferencia de SQL string en JDBC, las entidades JPA proveen validacion en compilacion.

**Queries clave:**

- `ExpenseRepository.findAllWithFilters()` - Filtrado dinamico con 6 parametros opcionales
- `ExpenseRepository.sumByCategoryAndDateRange()` - Agregacion para desglose por categoria
- `ExpenseRepository.monthlyTrend()` - Agrupacion por ano/mes para tendencias
- `CategoryRepository.findAllByUserIdOrDefault()` - Union de categorias default y del usuario

**Consecuencias:**

- JPQL no soporta todas las funciones avanzadas de SQL nativo (ventanas, CTEs). Si se necesitan, se puede usar `@Query(nativeQuery = true)`.
- Las entidades JPA con relaciones Lazy requieren atencion para evitar N+1 (resuelto con JOIN FETCH).
- El `findAllWithFilters` usa parametros nullable en JPQL (`IS NULL OR`), lo cual funciona pero podria optimizarse con Criteria API si la complejidad crece.
