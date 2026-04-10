# Arquitectura

## Vision General

ExpenseWise utiliza una arquitectura de capas (Layered Architecture) sobre Spring Boot 3.2 con Java 21.

## Capas del Sistema

### 1. Capa de Presentacion (Controllers)

Los controllers son delegadores puros. No contienen logica de negocio.

**Responsabilidades:**
- Recibir peticiones HTTP
- Validar con `@Valid` (Bean Validation)
- Delegar al servicio correspondiente
- Retornar `ResponseEntity` con el codigo de estado correcto

**Controllers:**
- `AuthController` - POST /register, /login
- `ExpenseController` - CRUD de gastos con filtros y paginacion
- `CategoryController` - CRUD de categorias
- `DashboardController` - Metricas y analitica
- `ExportController` - CSV y PDF

### 2. Capa de Seguridad

Implementada con Spring Security y JWT.

**Componentes:**
- `SecurityConfig` - Configura la cadena de filtros, CORS y endpoints publicos
- `JwtAuthenticationFilter` - Extrae y valida tokens JWT de cada request
- `JwtTokenProvider` - Genera y valida tokens con Clock inyectable
- `CustomUserDetailsService` - Carga usuarios por email desde la BD
- `SecurityUserContext` - Abstrae el acceso al usuario autenticado actual

### 3. Capa de Negocio (Services)

Toda la logica de negocio vive en los services.

**Responsabilidades:**
- Validaciones de reglas de negocio (ej: categorias default no editables)
- Transaccionalidad (`@Transactional` en operaciones de escritura)
- Aislamiento de datos por usuario
- Conversion entre entidades y DTOs
- Generacion de reportes (CSV, PDF)

### 4. Capa de Acceso a Datos (Repositories)

Spring Data JPA con queries JPQL personalizadas.

**Tecnicas utilizadas:**
- Queries derivadas para operaciones simples
- `@Query` con JPQL para filtros complejos
- `JOIN FETCH` para evitar N+1
- `Page<>` y `Pageable` para paginacion nativa

### 5. Capa de Persistencia

- **Produccion:** PostgreSQL 16 con Flyway
- **Desarrollo:** H2 in-memory en modo PostgreSQL
- **Testing:** H2 in-memory con create-drop

## Capa Transversal: Manejo de Errores

`GlobalExceptionHandler` con `@RestControllerAdvice` centraliza el manejo de excepciones:

| Excepcion | Codigo HTTP |
|-----------|------------|
| `ResourceNotFoundException` | 404 |
| `DuplicateResourceException` | 409 |
| `UnauthorizedException` | 401 |
| `BadCredentialsException` | 401 |
| `MethodArgumentNotValidException` | 400 |
| `IllegalArgumentException` | 400 |
| `Exception` (general) | 500 |

## Principios SOLID

1. **SRP**: Controllers delegan, services procesan, repositories consultan. DTOs separados de entidades.
2. **OCP**: Nuevas funcionalidades como nuevos controllers/services sin modificar los existentes.
3. **LSP**: Excepciones personalizadas extienden RuntimeException correctamente.
4. **ISP**: Interfaces de repository minimas. DTOs de request/response separados por dominio.
5. **DIP**: Inyeccion por constructor en toda la aplicacion. Clock inyectable para testing.
