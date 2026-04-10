# Arquitectura de ExpenseWise

## Vision General

ExpenseWise utiliza una arquitectura de capas (Layered Architecture) implementada sobre Spring Boot 3.2 con Java 21. El diseno prioriza la separacion de responsabilidades, la testabilidad y la mantenibilidad a largo plazo.

---

## Diagrama de Capas

```
+================================================================+
|                        CAPA DE PRESENTACION                     |
|                                                                 |
|  +-----------+  +-----------+  +-----------+  +-----------+    |
|  |   Auth    |  |  Expense  |  | Category  |  | Dashboard |    |
|  | Controller|  | Controller|  | Controller|  | Controller|    |
|  +-----------+  +-----------+  +-----------+  +-----------+    |
|  +-----------+                                                  |
|  |  Export   |  - Delegacion pura al servicio                   |
|  | Controller|  - Validacion declarativa con @Valid             |
|  +-----------+  - Sin logica de negocio                         |
|                                                                 |
+========================== | ====================================+
                            |
+========================== v ====================================+
|                        CAPA DE SEGURIDAD                        |
|                                                                 |
|  +---------------------+  +---------------------------+        |
|  | JwtAuthFilter       |  | SecurityConfig            |        |
|  | (OncePerRequestFilter) | (SecurityFilterChain)     |        |
|  +---------------------+  +---------------------------+        |
|  +---------------------+  +---------------------------+        |
|  | JwtTokenProvider    |  | CustomUserDetailsService  |        |
|  | (generacion/valid.) |  | (carga por email)         |        |
|  +---------------------+  +---------------------------+        |
|  +---------------------+                                       |
|  | SecurityUserContext  |  - Abstrae acceso al usuario actual   |
|  +---------------------+                                       |
|                                                                 |
+========================== | ====================================+
                            |
+========================== v ====================================+
|                     CAPA DE NEGOCIO (Services)                  |
|                                                                 |
|  +-------------+  +--------------+  +---------------+          |
|  | AuthService |  | ExpenseService|  | CategoryService|         |
|  +-------------+  +--------------+  +---------------+          |
|  +----------------+  +---------------+                          |
|  | DashboardService|  | ExportService |                         |
|  +----------------+  +---------------+                          |
|                                                                 |
|  - Logica de negocio centralizada                               |
|  - @Transactional en operaciones de escritura                   |
|  - Validaciones de reglas de negocio                            |
|  - Aislamiento de datos por usuario                             |
|                                                                 |
+========================== | ====================================+
                            |
+========================== v ====================================+
|                     CAPA DE ACCESO A DATOS                      |
|                                                                 |
|  +----------------+  +------------------+  +-----------------+ |
|  | UserRepository |  | ExpenseRepository|  | CategoryRepository|
|  +----------------+  +------------------+  +-----------------+ |
|                                                                 |
|  - Spring Data JPA con queries JPQL                             |
|  - JOIN FETCH para evitar N+1                                   |
|  - Paginacion nativa con Page<> y Pageable                     |
|  - Queries de agregacion para dashboard                         |
|                                                                 |
+========================== | ====================================+
                            |
+========================== v ====================================+
|                     CAPA DE PERSISTENCIA                        |
|                                                                 |
|  +----------------+          +------------------+               |
|  | PostgreSQL 16  |          | H2 in-memory     |               |
|  | (produccion)   |          | (desarrollo/test)|               |
|  +----------------+          +------------------+               |
|                                                                 |
|  Flyway: V1-V4 migraciones versionadas (solo produccion)       |
|  H2: create-drop con data.sql seed (solo desarrollo)           |
|                                                                 |
+=================================================================+
```

---

## Capa Transversal: Manejo de Errores

```
+-------------------------------------------------------+
|              GlobalExceptionHandler                    |
|              (@RestControllerAdvice)                   |
|                                                        |
|  ResourceNotFoundException  --> 404 Not Found           |
|  DuplicateResourceException --> 409 Conflict            |
|  UnauthorizedException      --> 401 Unauthorized        |
|  BadCredentialsException    --> 401 Unauthorized        |
|  MethodArgumentNotValid     --> 400 Bad Request          |
|  IllegalArgumentException   --> 400 Bad Request          |
|  Exception (general)        --> 500 Internal Server Error|
+-------------------------------------------------------+

Respuesta unificada: ErrorResponse (record)
{
  "status": int,
  "message": String,
  "errors": Map<String,String>  (nullable, solo validaciones),
  "timestamp": Instant
}
```

---

## Flujo de Autenticacion JWT

```
1. REGISTRO / LOGIN
   Cliente --> POST /api/auth/register o /login
                     |
                     v
              AuthController
                     |
                     v
              AuthService
              - Valida credenciales
              - BCrypt para hash
              - Genera JWT via JwtTokenProvider
                     |
                     v
              Retorna: { token, email, name }

2. PETICION AUTENTICADA
   Cliente --> GET /api/expenses (Header: Authorization: Bearer <token>)
                     |
                     v
           JwtAuthenticationFilter (OncePerRequestFilter)
              - Extrae token del header
              - Valida con JwtTokenProvider
              - Carga UserDetails
              - Establece SecurityContext
                     |
                     v
           ExpenseController --> ExpenseService
              - SecurityUserContext.getCurrentUser()
              - Opera solo con datos del usuario autenticado
```

---

## Flujo de Datos: Crear un Gasto

```
HTTP POST /api/expenses
  Body: { categoryId, amount, description, expenseDate }
  Header: Authorization: Bearer <jwt>
          |
          v
  JwtAuthenticationFilter
    - Valida token JWT
    - Establece SecurityContext
          |
          v
  ExpenseController.createExpense(@Valid @RequestBody ExpenseRequest)
    - Bean Validation automatica (amount > 0, date not null, etc.)
    - Si falla: MethodArgumentNotValidException --> 400
          |
          v
  ExpenseService.createExpense(request)
    - SecurityUserContext.getCurrentUser() --> obtiene User
    - CategoryRepository.findByIdAndUserIdOrDefault() --> valida categoria
    - Construye Expense via Builder
    - ExpenseRepository.save(expense)
    - Convierte a ExpenseResponse (record)
          |
          v
  ResponseEntity.status(201).body(response)
    --> { id, categoryId, categoryName, categoryIcon, amount, description, expenseDate, ... }
```

---

## Dependencias entre Componentes

```
AuthController -----> AuthService -----> UserRepository
                                  -----> JwtTokenProvider
                                  -----> PasswordEncoder
                                  -----> AuthenticationManager

ExpenseController --> ExpenseService --> ExpenseRepository
                                   --> CategoryRepository
                                   --> SecurityUserContext

CategoryController -> CategoryService -> CategoryRepository
                                    --> SecurityUserContext

DashboardController -> DashboardService -> ExpenseRepository
                                      --> SecurityUserContext

ExportController --> ExportService --> ExpenseRepository
                                 --> SecurityUserContext
```

---

## Perfiles de Configuracion

| Perfil | Base de Datos | Flyway | Puerto | Uso |
|--------|--------------|--------|--------|-----|
| `dev` (default) | H2 in-memory (modo PG) | Desactivado | 3001 | Desarrollo local |
| `test` | H2 in-memory | Desactivado | - | Tests automatizados |
| `prod` | PostgreSQL 16 | Activado | 8080 | Produccion |

---

## Decisiones de Diseno Clave

1. **Entidades JPA separadas de DTOs**: Las entidades de dominio (`User`, `Expense`, `Category`) nunca se exponen directamente. Todos los endpoints usan records inmutables como DTOs.

2. **SecurityUserContext como abstraccion**: En lugar de acceder al `SecurityContextHolder` directamente en cada servicio, se usa un componente dedicado que encapsula la obtencion del usuario autenticado.

3. **Clock inyectable**: El `JwtTokenProvider` recibe un `Clock` por constructor, facilitando el testing de escenarios de expiracion sin depender del reloj del sistema.

4. **Queries con JOIN FETCH**: Las consultas en `ExpenseRepository` usan `JOIN FETCH e.category` para cargar la categoria en una sola query, evitando el problema N+1.

5. **Indice compuesto user_date**: El indice `idx_expenses_user_date ON expenses(user_id, expense_date)` optimiza las consultas del dashboard que filtran por usuario y rango de fechas.
