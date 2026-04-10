<p align="center">
  <h1 align="center">ExpenseWise</h1>
  <p align="center">Control inteligente de gastos para freelancers y profesionales independientes</p>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk" />
  <img src="https://img.shields.io/badge/Spring_Boot-3.2-6DB33F?style=for-the-badge&logo=springboot" />
  <img src="https://img.shields.io/badge/PostgreSQL-16-4169E1?style=for-the-badge&logo=postgresql" />
  <img src="https://img.shields.io/badge/License-MIT-blue?style=for-the-badge" />
</p>

<p align="center">
  <img src="https://img.shields.io/github/actions/workflow/status/ronalc90/expense-wise/ci.yml?branch=main&style=flat-square&label=CI" />
  <img src="https://img.shields.io/badge/tests-39%20passing-brightgreen?style=flat-square" />
  <img src="https://img.shields.io/badge/coverage-service%20%2B%20integration-blue?style=flat-square" />
</p>

---

## Sobre el Proyecto

**ExpenseWise** es una API REST completa para el control y seguimiento de gastos personales, disenada especificamente para freelancers, trabajadores independientes y pequenos negocios.

El problema es claro: los profesionales independientes pierden tiempo valioso rastreando gastos en hojas de calculo, pierden recibos y sufren al momento de preparar declaraciones fiscales. Las soluciones existentes como QuickBooks o Expensify son demasiado complejas o costosas para usuarios individuales.

ExpenseWise resuelve esto con una API ligera, segura y bien estructurada que permite:

- Registrar gastos en segundos con categorizacion automatica
- Visualizar patrones de gasto a traves de un dashboard analitico
- Exportar reportes en CSV y PDF listos para el contador
- Gestionar categorias personalizadas junto con 14 categorias predeterminadas

El frontend incluido presenta una interfaz moderna con efecto glassmorphism oscuro, completamente responsive y funcional desde el navegador.

### Construido con

- [Spring Boot 3.2](https://spring.io/projects/spring-boot) - Framework backend
- [Java 21](https://openjdk.org/projects/jdk/21/) - Lenguaje de programacion
- [PostgreSQL 16](https://www.postgresql.org/) - Base de datos en produccion
- [H2 Database](https://www.h2database.com/) - Base de datos en desarrollo/testing
- [Flyway](https://flywaydb.org/) - Migraciones de base de datos
- [JWT (jjwt)](https://github.com/jwtk/jjwt) - Autenticacion stateless

---

## Capturas de Pantalla

> Las capturas de pantalla del frontend se agregaran proximamente. El proyecto incluye una interfaz web con efecto glassmorphism oscuro servida directamente desde Spring Boot.

```
+--------------------------------------------------+
|  ExpenseWise - Dashboard                         |
|                                                  |
|  Total: $1,250.00  |  Promedio: $45.00           |
|  Transacciones: 28 |  Mayor: $200.00             |
|                                                  |
|  [Grafico de barras por categoria]               |
|  [Tendencia mensual]                             |
+--------------------------------------------------+
```

---

## Caracteristicas

### Autenticacion y Seguridad
- **Registro y login** con validacion completa de datos
- **JWT (JSON Web Tokens)** para autenticacion stateless
- **Hashing BCrypt** para contrasenas
- **Endpoints protegidos** - toda operacion de datos requiere autenticacion
- **CORS configurado** para integracion con frontends externos

### Gestion de Gastos
- **CRUD completo** de gastos con validaciones declarativas
- **Filtrado avanzado** por categoria, rango de fechas y monto
- **Paginacion** configurable con ordenamiento por fecha
- **Aislamiento por usuario** - cada usuario solo ve sus propios gastos

### Categorias
- **14 categorias predeterminadas** disponibles para todos los usuarios (Food & Dining, Transportation, Housing, etc.)
- **Categorias personalizadas** por usuario con iconos configurables
- **Proteccion de categorias default** - no se pueden modificar ni eliminar
- **Validacion de unicidad** por nombre y usuario

### Dashboard Analitico
- **Resumen financiero** con total, promedio, conteo y gasto maximo
- **Desglose por categoria** con porcentajes relativos
- **Tendencia mensual** para analisis temporal de gastos
- **Consultas por rango de fechas** completamente flexible

### Exportacion de Reportes
- **Exportacion CSV** con formato listo para importar en Excel
- **Exportacion PDF** con tabla profesional estilizada (iTextPDF)
- **Filtrado por periodo** en ambos formatos

### Frontend Integrado
- **Interfaz glassmorphism** oscura servida como recurso estatico
- **Single Page Application** sin dependencias de build
- **Responsive design** adaptado a desktop y movil

---

## Arquitectura

El proyecto sigue una arquitectura de capas limpia, aplicando principios SOLID de forma consistente:

```
                    +-------------------+
                    |    Frontend SPA   |
                    |  (Static HTML/JS) |
                    +--------+----------+
                             |
                             | HTTP/REST + JWT
                             v
+------------------------------------------------------------+
|                     SPRING BOOT APPLICATION                |
|                                                            |
|  +------------------+    +-----------------------------+   |
|  |   Security Layer |    |      @RestControllerAdvice  |   |
|  |  JwtAuthFilter   |    |   GlobalExceptionHandler    |   |
|  |  SecurityConfig  |    +-----------------------------+   |
|  +--------+---------+                                      |
|           |                                                |
|           v                                                |
|  +------------------+                                      |
|  |   Controllers    |  Delegacion pura, sin logica de      |
|  |  AuthController  |  negocio. Valida con @Valid y        |
|  |  ExpenseCtrl     |  delega al servicio correspondiente. |
|  |  CategoryCtrl    |                                      |
|  |  DashboardCtrl   |                                      |
|  |  ExportCtrl      |                                      |
|  +--------+---------+                                      |
|           |                                                |
|           v                                                |
|  +------------------+                                      |
|  |    Services      |  Logica de negocio centralizada.     |
|  |  AuthService     |  @Transactional en escrituras.       |
|  |  ExpenseService  |  Validaciones de reglas de negocio.  |
|  |  CategoryService |  Aislamiento por usuario.            |
|  |  DashboardSvc    |                                      |
|  |  ExportService   |                                      |
|  +--------+---------+                                      |
|           |                                                |
|           v                                                |
|  +------------------+                                      |
|  |  Repositories    |  Acceso a datos via Spring Data JPA. |
|  |  UserRepo        |  Queries JPQL optimizadas con        |
|  |  ExpenseRepo     |  JOIN FETCH para evitar N+1.         |
|  |  CategoryRepo    |                                      |
|  +--------+---------+                                      |
|           |                                                |
+-----------|------------------------------------------------+
            |
            v
    +-------+--------+
    |   PostgreSQL    |  Produccion: PostgreSQL 16
    |   / H2 (dev)   |  Desarrollo: H2 in-memory (modo PG)
    +----------------+
```

### Principios SOLID Aplicados

| Principio | Implementacion |
|-----------|---------------|
| **Single Responsibility** | Cada clase tiene una unica responsabilidad: controllers solo delegan, services contienen logica de negocio, repositories acceden a datos. DTOs (records) separados de entidades JPA. |
| **Open/Closed** | `GlobalExceptionHandler` permite agregar manejo de nuevas excepciones sin modificar las existentes. Nuevos endpoints se agregan como nuevos controllers. |
| **Liskov Substitution** | Las excepciones personalizadas (`ResourceNotFoundException`, `DuplicateResourceException`) extienden `RuntimeException` correctamente. |
| **Interface Segregation** | Repositories exponen solo los metodos necesarios. DTOs de request y response son independientes para cada dominio. |
| **Dependency Inversion** | Inyeccion por constructor en toda la aplicacion. `SecurityUserContext` abstrae el acceso al usuario autenticado. `Clock` inyectable para testing. |

---

## Modelo de Datos

```
+------------------+       +-------------------+       +------------------+
|      users       |       |    categories     |       |     expenses     |
+------------------+       +-------------------+       +------------------+
| id          PK   |<------| user_id    FK     |       | id          PK   |
| email     UNIQUE |       | id          PK    |<------| category_id FK   |
| password_hash    |       | name              |       | user_id     FK   |---+
| name             |       | icon              |       | amount           |   |
| currency (3)     |       | is_default BOOL   |       | description      |   |
| created_at       |       | created_at        |       | expense_date     |   |
+------------------+       | UNIQUE(name,      |       | receipt_url      |   |
        ^                  |   user_id)        |       | created_at       |   |
        |                  +-------------------+       | updated_at       |   |
        |                                              +------------------+   |
        +-------------------------------------------------------------+------+
```

### Entidades

| Entidad | Descripcion | Campos Clave |
|---------|------------|-------------|
| **User** | Representa a un usuario registrado en el sistema. Cada usuario tiene un email unico y una moneda preferida. | `email` (unique), `password_hash` (BCrypt), `currency` (default: USD) |
| **Category** | Categorias para clasificar gastos. Pueden ser predeterminadas (compartidas) o personalizadas por usuario. | `is_default` distingue categorias del sistema vs. del usuario. Constraint UNIQUE en `(name, user_id)` |
| **Expense** | Registro individual de un gasto. Siempre pertenece a un usuario y una categoria. | `amount` (DECIMAL 12,2), `expense_date` para el filtrado temporal, timestamps de auditoria |

### Indices de Base de Datos

```sql
idx_users_email          ON users(email)
idx_categories_user_id   ON categories(user_id)
idx_expenses_user_id     ON expenses(user_id)
idx_expenses_category_id ON expenses(category_id)
idx_expenses_expense_date ON expenses(expense_date)
idx_expenses_user_date   ON expenses(user_id, expense_date)  -- Indice compuesto para dashboard
```

---

## Stack Tecnologico

| Tecnologia | Version | Proposito |
|-----------|---------|----------|
| Java | 21 (LTS) | Lenguaje principal con records, pattern matching y sealed classes |
| Spring Boot | 3.2.5 | Framework backend con auto-configuracion |
| Spring Security | 6.x | Autenticacion y autorizacion |
| Spring Data JPA | 3.2.x | Acceso a datos con repositories |
| Hibernate | 6.x | ORM y generacion de queries |
| PostgreSQL | 16 | Base de datos relacional en produccion |
| H2 Database | 2.x | Base de datos in-memory para desarrollo y testing |
| Flyway | 9.x | Migraciones versionadas de esquema |
| jjwt | 0.12.5 | Generacion y validacion de tokens JWT |
| Lombok | 1.18.44 | Reduccion de boilerplate (getters, builders) |
| MapStruct | 1.6.3 | Mapeo type-safe entre DTOs y entidades |
| SpringDoc OpenAPI | 2.5.0 | Documentacion automatica de API (Swagger UI) |
| iTextPDF | 5.5.13 | Generacion de reportes PDF |
| JUnit 5 | 5.x | Framework de testing |
| Mockito | 5.18.0 | Mocking para tests unitarios |
| Docker | - | Contenedorizacion de la aplicacion |
| Maven | 3.9+ | Gestion de dependencias y build |

---

## Prerequisitos

Antes de comenzar, asegurate de tener instalado:

- **Java 21** (JDK) - [Descargar Temurin](https://adoptium.net/)
- **Maven 3.9+** - [Descargar Maven](https://maven.apache.org/download.cgi)
- **Docker y Docker Compose** (opcional, para PostgreSQL) - [Descargar Docker](https://www.docker.com/)
- **Git** - [Descargar Git](https://git-scm.com/)

Verificar instalacion:

```bash
java --version    # java 21.x.x
mvn --version     # Apache Maven 3.9.x
docker --version  # Docker version 24.x+ (opcional)
```

---

## Instalacion y Configuracion

### 1. Clonar el repositorio

```bash
git clone https://github.com/ronalc90/expense-wise.git
cd expense-wise
```

### 2. Modo Desarrollo (H2 in-memory)

No necesitas configurar base de datos. El perfil `dev` usa H2 automaticamente:

```bash
# Compilar y ejecutar tests
mvn clean verify

# Ejecutar la aplicacion
mvn spring-boot:run
```

La aplicacion estara disponible en:
- **API:** http://localhost:3001
- **Swagger UI:** http://localhost:3001/swagger-ui.html
- **H2 Console:** http://localhost:3001/h2-console
- **Frontend:** http://localhost:3001/

### 3. Modo Produccion (PostgreSQL)

```bash
# Iniciar PostgreSQL con Docker
docker compose up -d postgres

# Configurar variables de entorno
export SPRING_PROFILES_ACTIVE=prod
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/expensewise
export SPRING_DATASOURCE_USERNAME=expensewise
export SPRING_DATASOURCE_PASSWORD=expensewise
export JWT_SECRET=tu-clave-secreta-de-al-menos-256-bits-para-HS256

# Compilar y ejecutar
mvn clean package -DskipTests
java -jar target/expensewise-1.0.0-SNAPSHOT.jar
```

### 4. Docker Compose completo

```bash
# Construir la imagen
mvn clean package -DskipTests
docker build -t expensewise:latest .

# Iniciar todo el stack
docker compose up -d
```

---

## Variables de Entorno

| Variable | Descripcion | Valor por defecto |
|----------|-----------|-------------------|
| `SPRING_PROFILES_ACTIVE` | Perfil activo de Spring | `dev` |
| `SPRING_DATASOURCE_URL` | URL de conexion a la base de datos | `jdbc:h2:mem:expensewise` (dev) |
| `SPRING_DATASOURCE_USERNAME` | Usuario de base de datos | `sa` (dev) |
| `SPRING_DATASOURCE_PASSWORD` | Contrasena de base de datos | *(vacio en dev)* |
| `JWT_SECRET` | Clave secreta para firmar tokens JWT (min. 256 bits) | Clave por defecto solo para desarrollo |
| `JWT_EXPIRATION_MS` | Tiempo de expiracion del token en milisegundos | `86400000` (24 horas) |
| `SERVER_PORT` | Puerto del servidor | `8080` (prod), `3001` (dev) |

> **Importante:** En produccion, SIEMPRE configura un `JWT_SECRET` unico y seguro. Nunca uses la clave por defecto.

---

## Documentacion de la API

Base URL: `http://localhost:3001/api` (dev) | `http://localhost:8080/api` (prod)

La documentacion interactiva completa esta disponible en `/swagger-ui.html` cuando la aplicacion esta corriendo.

Para documentacion detallada con ejemplos curl de cada endpoint, consulta [docs/API.md](docs/API.md).

### Autenticacion

| Metodo | Endpoint | Auth | Descripcion |
|--------|----------|------|-------------|
| `POST` | `/api/auth/register` | No | Registrar un nuevo usuario |
| `POST` | `/api/auth/login` | No | Iniciar sesion y obtener token JWT |

#### POST /api/auth/register

```bash
curl -X POST http://localhost:3001/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Ronald",
    "email": "ronald@example.com",
    "password": "miPassword123",
    "currency": "USD"
  }'
```

Respuesta (201 Created):
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "email": "ronald@example.com",
  "name": "Ronald"
}
```

#### POST /api/auth/login

```bash
curl -X POST http://localhost:3001/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "ronald@example.com",
    "password": "miPassword123"
  }'
```

### Gastos

| Metodo | Endpoint | Auth | Descripcion |
|--------|----------|------|-------------|
| `GET` | `/api/expenses` | Si | Listar gastos con filtros y paginacion |
| `GET` | `/api/expenses/{id}` | Si | Obtener un gasto por ID |
| `POST` | `/api/expenses` | Si | Crear un nuevo gasto |
| `PUT` | `/api/expenses/{id}` | Si | Actualizar un gasto existente |
| `DELETE` | `/api/expenses/{id}` | Si | Eliminar un gasto |

**Filtros disponibles:** `categoryId`, `startDate`, `endDate`, `minAmount`, `maxAmount`, `page`, `size`, `sort`

### Categorias

| Metodo | Endpoint | Auth | Descripcion |
|--------|----------|------|-------------|
| `GET` | `/api/categories` | Si | Listar categorias (default + personalizadas) |
| `GET` | `/api/categories/{id}` | Si | Obtener una categoria por ID |
| `POST` | `/api/categories` | Si | Crear una categoria personalizada |
| `PUT` | `/api/categories/{id}` | Si | Actualizar una categoria (solo personalizadas) |
| `DELETE` | `/api/categories/{id}` | Si | Eliminar una categoria (solo personalizadas) |

### Dashboard

| Metodo | Endpoint | Auth | Descripcion |
|--------|----------|------|-------------|
| `GET` | `/api/dashboard/summary` | Si | Resumen financiero del periodo |
| `GET` | `/api/dashboard/by-category` | Si | Desglose de gastos por categoria |
| `GET` | `/api/dashboard/monthly-trend` | Si | Tendencia mensual de gastos |

**Parametros requeridos:** `startDate`, `endDate` (formato: `YYYY-MM-DD`)

### Exportacion

| Metodo | Endpoint | Auth | Descripcion |
|--------|----------|------|-------------|
| `GET` | `/api/export/csv` | Si | Exportar gastos a CSV |
| `GET` | `/api/export/pdf` | Si | Exportar gastos a PDF |

**Parametros requeridos:** `startDate`, `endDate` (formato: `YYYY-MM-DD`)

### Codigos de Error

| Codigo | Significado |
|--------|------------|
| `400` | Validacion fallida o argumento invalido |
| `401` | Credenciales invalidas o token expirado |
| `403` | Acceso denegado (sin token) |
| `404` | Recurso no encontrado |
| `409` | Recurso duplicado (email o nombre de categoria) |
| `500` | Error interno del servidor |

> Todos los endpoints (excepto `/api/auth/**`) requieren el header: `Authorization: Bearer <token>`

---

## Estructura del Proyecto

```
expense-wise/
├── src/
│   ├── main/
│   │   ├── java/com/expensewise/
│   │   │   ├── ExpenseWiseApplication.java          # Punto de entrada
│   │   │   ├── config/
│   │   │   │   ├── AppConfig.java                   # Bean Clock + ConfigProperties
│   │   │   │   ├── JwtProperties.java               # Record de configuracion JWT
│   │   │   │   └── SecurityConfig.java              # Cadena de filtros, CORS, BCrypt
│   │   │   ├── controller/
│   │   │   │   ├── AuthController.java              # POST /register, /login
│   │   │   │   ├── ExpenseController.java           # CRUD gastos con filtros
│   │   │   │   ├── CategoryController.java          # CRUD categorias
│   │   │   │   ├── DashboardController.java         # Analitica y reportes
│   │   │   │   └── ExportController.java            # CSV y PDF export
│   │   │   ├── service/
│   │   │   │   ├── AuthService.java                 # Registro, login, JWT
│   │   │   │   ├── ExpenseService.java              # Logica de gastos
│   │   │   │   ├── CategoryService.java             # Logica de categorias
│   │   │   │   ├── DashboardService.java            # Agregaciones y metricas
│   │   │   │   └── ExportService.java               # Generacion CSV/PDF
│   │   │   ├── domain/
│   │   │   │   ├── entity/
│   │   │   │   │   ├── User.java                    # Entidad JPA de usuarios
│   │   │   │   │   ├── Category.java                # Entidad JPA de categorias
│   │   │   │   │   └── Expense.java                 # Entidad JPA de gastos
│   │   │   │   └── repository/
│   │   │   │       ├── UserRepository.java          # Acceso a datos de usuarios
│   │   │   │       ├── CategoryRepository.java      # Queries de categorias
│   │   │   │       └── ExpenseRepository.java       # Queries complejas de gastos
│   │   │   ├── dto/
│   │   │   │   ├── auth/                            # RegisterRequest, LoginRequest, AuthResponse
│   │   │   │   ├── expense/                         # ExpenseRequest, ExpenseResponse
│   │   │   │   ├── category/                        # CategoryRequest, CategoryResponse
│   │   │   │   └── dashboard/                       # DashboardSummary, CategoryBreakdown, MonthlyTrend
│   │   │   ├── security/
│   │   │   │   ├── JwtTokenProvider.java            # Generacion/validacion de tokens
│   │   │   │   ├── JwtAuthenticationFilter.java     # Filtro de autenticacion HTTP
│   │   │   │   ├── CustomUserDetailsService.java    # Carga de usuario por email
│   │   │   │   └── SecurityUserContext.java         # Acceso al usuario autenticado
│   │   │   └── exception/
│   │   │       ├── GlobalExceptionHandler.java      # @RestControllerAdvice centralizado
│   │   │       ├── ErrorResponse.java               # Record de respuesta de error
│   │   │       ├── ResourceNotFoundException.java   # 404
│   │   │       ├── DuplicateResourceException.java  # 409
│   │   │       └── UnauthorizedException.java       # 401
│   │   └── resources/
│   │       ├── application.yml                      # Configuracion base
│   │       ├── application-dev.yml                  # Perfil desarrollo (H2)
│   │       ├── data.sql                             # Datos seed para H2
│   │       ├── static/
│   │       │   └── index.html                       # Frontend glassmorphism
│   │       └── db/migration/
│   │           ├── V1__create_users.sql
│   │           ├── V2__create_categories.sql
│   │           ├── V3__create_expenses.sql
│   │           └── V4__seed_default_categories.sql
│   └── test/
│       ├── java/com/expensewise/
│       │   ├── integration/
│       │   │   └── BaseIntegrationTest.java         # Clase base con MockMvc y JWT
│       │   ├── controller/                          # 27 tests de integracion
│       │   └── service/                             # 12 tests unitarios
│       └── resources/
│           └── application-test.yml                 # Config para tests (H2)
├── docs/                                            # Documentacion extendida
├── .github/workflows/ci.yml                         # Pipeline CI/CD
├── pom.xml
├── Dockerfile
├── docker-compose.yml
├── CONTRIBUTING.md
├── CHANGELOG.md
└── LICENSE
```

---

## Decisiones Tecnicas

Para documentacion detallada de cada decision en formato ADR, consulta [docs/DECISIONS.md](docs/DECISIONS.md).

| Decision | Eleccion | Razon Principal |
|----------|---------|----------------|
| Framework backend | Spring Boot 3.2 | Ecosistema maduro, auto-configuracion, soporte Java 21 |
| Autenticacion | JWT (jjwt 0.12.5) | Stateless, escalable horizontalmente, estandar abierto |
| DTOs | Java Records | Inmutabilidad garantizada, reduccion de boilerplate |
| BD desarrollo | H2 in-memory | Cero configuracion, tests rapidos, modo PostgreSQL |
| BD produccion | PostgreSQL 16 | Robustez, ACID completo, indices avanzados |
| Migraciones | Flyway | Scripts SQL versionados, control total del esquema |
| Acceso a datos | Spring Data JPA | Queries derivadas + JPQL personalizado, paginacion nativa |

---

## Testing

### Ejecutar todos los tests

```bash
mvn clean verify
```

### Ejecutar solo tests unitarios

```bash
mvn test -Dtest="*ServiceTest"
```

### Ejecutar solo tests de integracion

```bash
mvn test -Dtest="*ControllerTest"
```

### Resumen de Tests

| Clase de Test | Tests | Tipo | Cobertura |
|--------------|-------|------|-----------|
| `AuthControllerTest` | 7 | Integracion | Registro, login, validaciones, duplicados |
| `ExpenseControllerTest` | 8 | Integracion | CRUD, paginacion, autorizacion, validaciones |
| `CategoryControllerTest` | 8 | Integracion | CRUD, defaults, duplicados, autorizacion |
| `DashboardControllerTest` | 4 | Integracion | Summary, breakdown, trends, rango vacio |
| `ExpenseServiceTest` | 7 | Unitario | Crear, leer, actualizar, eliminar, errores |
| `AuthServiceTest` | 5 | Unitario | Registro, login, duplicados, defaults |
| **Total** | **39** | | |

---

## Despliegue

### Docker

```bash
# 1. Compilar la aplicacion
mvn clean package -DskipTests

# 2. Construir la imagen
docker build -t expensewise:latest .

# 3. Ejecutar con Docker Compose
docker compose up -d

# 4. Verificar
docker compose ps
docker compose logs -f
```

### Produccion (Railway / Render)

1. Conectar el repositorio de GitHub
2. Configurar las variables de entorno de produccion
3. Comando de build: `mvn clean package -DskipTests`
4. Comando de inicio: `java -jar target/expensewise-1.0.0-SNAPSHOT.jar`
5. Puerto: `8080`

Para instrucciones detalladas de despliegue, consulta [docs/DEPLOYMENT.md](docs/DEPLOYMENT.md).

---

## Roadmap

- [x] CRUD completo de gastos con validaciones
- [x] Autenticacion JWT con registro y login
- [x] 14 categorias predeterminadas + categorias personalizadas
- [x] Dashboard analitico (resumen, desglose por categoria, tendencia mensual)
- [x] Exportacion CSV y PDF
- [x] Frontend glassmorphism integrado
- [x] 39 tests (unitarios + integracion)
- [x] Documentacion Swagger/OpenAPI
- [x] CI/CD con GitHub Actions
- [ ] Escaneo de recibos con OCR
- [ ] Categorizacion automatica con ML
- [ ] Soporte multi-moneda con conversion automatica
- [ ] Gastos recurrentes (suscripciones)
- [ ] Presupuestos mensuales por categoria con alertas
- [ ] Integracion con bancos (Open Banking)
- [ ] Aplicacion movil (React Native / Flutter)
- [ ] Acceso para contadores con reportes fiscales

---

## Contribuir

Las contribuciones son bienvenidas. Consulta [CONTRIBUTING.md](CONTRIBUTING.md) para la guia completa.

1. Haz fork del proyecto
2. Crea tu rama de feature (`git checkout -b feature/nueva-funcionalidad`)
3. Commit con mensajes descriptivos (`git commit -m "feat: agregar soporte multi-moneda"`)
4. Push a la rama (`git push origin feature/nueva-funcionalidad`)
5. Abre un Pull Request

---

## Licencia

Distribuido bajo la Licencia MIT. Consulta [LICENSE](LICENSE) para mas informacion.

---

## Autor

**Ronald** - Desarrollo completo del backend, frontend, testing y documentacion.

- GitHub: [@ronalc90](https://github.com/ronalc90)

---

<p align="center">
  Si este proyecto te resulta util, considera darle una estrella en GitHub.
</p>
