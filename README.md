```
 ______                                __        ___
|  ____|                              \ \      / (_)
| |__  __  ___ __   ___ _ __  ___  ___ \ \ /\ / / _ ___  ___
|  __| \ \/ / '_ \ / _ \ '_ \/ __|/ _ \ \ \/  \/ / | / __|/ _ \
| |____ >  <| |_) |  __/ | | \__ \  __/  \  /\  /| |_\__ \  __/
|______/_/\_\ .__/ \___|_| |_|___/\___|   \/  \/ |_(_)___/\___|
             | |
             |_|
```

<div align="center">

# ExpenseWise

**Control inteligente de gastos para freelancers y profesionales independientes**

[![Java 21](https://img.shields.io/badge/Java-21-orange?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot 3](https://img.shields.io/badge/Spring%20Boot-3.2.5-6DB33F?logo=spring-boot&logoColor=white)](https://spring.io/projects/spring-boot)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-336791?logo=postgresql&logoColor=white)](https://www.postgresql.org/)

---

*Registra, categoriza y analiza tus gastos de forma rapida y sencilla.*
*Exporta reportes listos para tu contador en segundos.*

[Ver Demo en Vivo](#demo) | [Documentacion API](#documentacion-api) | [Comenzar](#como-ejecutar)

</div>

---

## Tabla de Contenidos

- [Acerca del Proyecto](#acerca-del-proyecto)
- [Capturas de Pantalla](#capturas-de-pantalla)
- [Funcionalidades](#funcionalidades)
- [Stack Tecnologico](#stack-tecnologico)
- [Arquitectura](#arquitectura)
- [Como Ejecutar](#como-ejecutar)
- [Documentacion API](#documentacion-api)
- [Estructura del Proyecto](#estructura-del-proyecto)
- [Variables de Entorno](#variables-de-entorno)
- [Contribuir](#contribuir)
- [Licencia](#licencia)

---

## Acerca del Proyecto

ExpenseWise nace de una necesidad real: **los freelancers y profesionales independientes pierden horas cada semana rastreando gastos**, organizando recibos y preparando reportes para sus contadores.

Las soluciones existentes como QuickBooks o Expensify son demasiado complejas o costosas para un profesional independiente. ExpenseWise ofrece una alternativa **simple, rapida y gratuita** que se enfoca en lo que realmente importa:

- Registrar un gasto en menos de 5 segundos
- Ver a donde se va tu dinero con dashboards claros
- Exportar todo listo para impuestos con un click

---

## Capturas de Pantalla

<div align="center">

> **Demo en vivo** - La interfaz utiliza un diseno glassmorphism oscuro con
> graficos interactivos y navegacion intuitiva.
>
> Ejecuta el proyecto localmente para explorar todas las funcionalidades.

</div>

---

## Funcionalidades

- [x] **Autenticacion JWT** - Registro y login seguro con tokens
- [x] **CRUD de Gastos** - Crear, leer, actualizar y eliminar gastos con filtros avanzados
- [x] **Gestion de Categorias** - Categorias personalizables con iconos y colores
- [x] **Dashboard Analitico** - Resumen de gastos, desglose por categoria y tendencias mensuales
- [x] **Exportacion CSV** - Descarga tus gastos en formato CSV para Excel
- [x] **Exportacion PDF** - Reportes profesionales listos para tu contador
- [x] **Filtros Avanzados** - Por fecha, categoria, rango de montos
- [x] **Paginacion** - Navegacion eficiente con paginacion server-side
- [x] **UI Glassmorphism** - Interfaz moderna con modo oscuro y graficos interactivos
- [x] **Swagger UI** - Documentacion interactiva de la API
- [x] **Docker Ready** - Despliegue con un solo comando
- [ ] Escaneo de recibos con OCR
- [ ] Categorizacion automatica
- [ ] Multi-moneda
- [ ] Integracion con bancos (Open Banking)

---

## Stack Tecnologico

| Capa | Tecnologia | Version |
|------|-----------|---------|
| **Lenguaje** | Java | 21 (LTS) |
| **Framework** | Spring Boot | 3.2.5 |
| **Seguridad** | Spring Security + JWT | jjwt 0.12.5 |
| **Persistencia** | Spring Data JPA + Hibernate | - |
| **Base de Datos** | PostgreSQL / H2 (dev) | 16 |
| **Migraciones** | Flyway | - |
| **Mapping** | MapStruct | 1.6.3 |
| **Documentacion** | SpringDoc OpenAPI | 2.5.0 |
| **PDF** | iTextPDF | 5.5.13 |
| **Build** | Maven | - |
| **Contenedores** | Docker + Docker Compose | - |
| **Frontend** | HTML + CSS + JavaScript | Vanilla |

---

## Arquitectura

```
                    +--------------------------------------------------+
                    |                   CLIENTE                         |
                    |        (HTML/CSS/JS - Glassmorphism UI)           |
                    +-------------------------+------------------------+
                                              |
                                         HTTP/REST
                                              |
                    +-------------------------v------------------------+
                    |               SPRING BOOT 3.2.5                  |
                    |                                                   |
                    |  +-------------+  +------------+  +------------+ |
                    |  |   Auth      |  |  Expense   |  | Dashboard  | |
                    |  | Controller  |  | Controller |  | Controller | |
                    |  +------+------+  +-----+------+  +-----+------+ |
                    |         |               |               |        |
                    |  +------v------+  +-----v------+  +-----v------+ |
                    |  |   Auth      |  |  Expense   |  | Dashboard  | |
                    |  |  Service    |  |  Service   |  |  Service   | |
                    |  +------+------+  +-----+------+  +-----+------+ |
                    |         |               |               |        |
                    |  +------v---------------v---------------v------+ |
                    |  |              JPA Repositories                | |
                    |  |   UserRepo | ExpenseRepo | CategoryRepo     | |
                    |  +------+------------------+-------------------+ |
                    |         |                  |                     |
                    +---------|------------------+---------------------+
                              |                  |
                    +---------v--+       +-------v--------+
                    | PostgreSQL |       |   H2 (dev)     |
                    |   (prod)   |       |  (in-memory)   |
                    +------------+       +----------------+

    Seguridad: JWT Filter --> SecurityConfig --> Endpoints protegidos
    Export:    ExportController --> ExportService --> CSV / PDF (iTextPDF)
```

---

## Como Ejecutar

### Prerequisitos

- **Java 21** (JDK) - [Descargar](https://adoptium.net/)
- **Maven 3.9+** - [Descargar](https://maven.apache.org/)
- **Docker** (opcional, para PostgreSQL) - [Descargar](https://www.docker.com/)

### Opcion 1: Modo desarrollo (H2 en memoria)

```bash
# Clonar el repositorio
git clone https://github.com/ronalc90/expense-wise.git
cd expense-wise

# Compilar y ejecutar
./mvnw spring-boot:run

# La aplicacion estara en http://localhost:3001
# Swagger UI: http://localhost:3001/swagger-ui.html
# H2 Console: http://localhost:3001/h2-console
```

### Opcion 2: Con Docker (PostgreSQL)

```bash
# Clonar el repositorio
git clone https://github.com/ronalc90/expense-wise.git
cd expense-wise

# Levantar PostgreSQL con Docker Compose
docker-compose up -d

# Configurar variables de entorno (ver seccion Variables de Entorno)
cp .env.example .env

# Compilar
./mvnw clean package -DskipTests

# Ejecutar con perfil de produccion
java -jar target/expensewise-1.0.0-SNAPSHOT.jar --spring.profiles.active=prod
```

### Opcion 3: Docker completo

```bash
# Construir imagen
docker build -t expensewise .

# Ejecutar con Docker Compose
docker-compose up -d
```

---

## Documentacion API

La documentacion interactiva esta disponible en `/swagger-ui.html` cuando la aplicacion esta corriendo.

### Autenticacion

| Metodo | Endpoint | Descripcion |
|--------|----------|-------------|
| `POST` | `/api/auth/register` | Registrar nuevo usuario |
| `POST` | `/api/auth/login` | Iniciar sesion (devuelve JWT) |

### Gastos

| Metodo | Endpoint | Descripcion |
|--------|----------|-------------|
| `GET` | `/api/expenses` | Listar gastos (paginado, con filtros) |
| `GET` | `/api/expenses/{id}` | Obtener gasto por ID |
| `POST` | `/api/expenses` | Crear nuevo gasto |
| `PUT` | `/api/expenses/{id}` | Actualizar gasto |
| `DELETE` | `/api/expenses/{id}` | Eliminar gasto |

**Filtros disponibles:** `categoryId`, `startDate`, `endDate`, `minAmount`, `maxAmount`

### Categorias

| Metodo | Endpoint | Descripcion |
|--------|----------|-------------|
| `GET` | `/api/categories` | Listar todas las categorias |
| `GET` | `/api/categories/{id}` | Obtener categoria por ID |
| `POST` | `/api/categories` | Crear categoria |
| `PUT` | `/api/categories/{id}` | Actualizar categoria |
| `DELETE` | `/api/categories/{id}` | Eliminar categoria |

### Dashboard

| Metodo | Endpoint | Descripcion |
|--------|----------|-------------|
| `GET` | `/api/dashboard/summary` | Resumen general de gastos |
| `GET` | `/api/dashboard/by-category` | Desglose por categoria |
| `GET` | `/api/dashboard/monthly-trend` | Tendencia mensual |

**Parametros requeridos:** `startDate`, `endDate` (formato: `YYYY-MM-DD`)

### Exportacion

| Metodo | Endpoint | Descripcion |
|--------|----------|-------------|
| `GET` | `/api/export/csv` | Exportar gastos a CSV |
| `GET` | `/api/export/pdf` | Exportar gastos a PDF |

**Parametros requeridos:** `startDate`, `endDate` (formato: `YYYY-MM-DD`)

> Todos los endpoints (excepto auth) requieren el header: `Authorization: Bearer <token>`

---

## Estructura del Proyecto

```
expense-wise/
├── src/
│   ├── main/
│   │   ├── java/com/expensewise/
│   │   │   ├── config/              # Configuracion (Security, JWT, App)
│   │   │   ├── controller/          # REST Controllers
│   │   │   │   ├── AuthController
│   │   │   │   ├── CategoryController
│   │   │   │   ├── DashboardController
│   │   │   │   ├── ExpenseController
│   │   │   │   └── ExportController
│   │   │   ├── domain/
│   │   │   │   ├── entity/          # Entidades JPA (User, Expense, Category)
│   │   │   │   └── repository/      # Spring Data Repositories
│   │   │   ├── dto/                 # Data Transfer Objects
│   │   │   │   ├── auth/            # Login/Register DTOs
│   │   │   │   ├── category/        # Category DTOs
│   │   │   │   ├── dashboard/       # Dashboard DTOs
│   │   │   │   └── expense/         # Expense DTOs
│   │   │   ├── exception/           # Manejo global de errores
│   │   │   ├── mapper/              # MapStruct mappers
│   │   │   ├── security/            # JWT Filter, Provider, UserDetails
│   │   │   └── service/             # Logica de negocio
│   │   └── resources/
│   │       ├── db/migration/        # Flyway migrations (V1-V4)
│   │       ├── static/              # Frontend (HTML/CSS/JS)
│   │       ├── application.yml      # Configuracion principal
│   │       └── application-dev.yml  # Configuracion desarrollo (H2)
│   └── test/
│       └── java/com/expensewise/
│           ├── controller/          # Tests de controllers
│           ├── integration/         # Tests de integracion
│           └── service/             # Tests de servicios
├── docs/                            # GitHub Pages
├── docker-compose.yml               # PostgreSQL + Redis
├── Dockerfile                       # Imagen de produccion
├── pom.xml                          # Dependencias Maven
└── README.md
```

---

## Variables de Entorno

Crea un archivo `.env` en la raiz del proyecto:

```env
# Base de datos (produccion)
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/expensewise
SPRING_DATASOURCE_USERNAME=expensewise
SPRING_DATASOURCE_PASSWORD=tu_password_seguro

# JWT
JWT_SECRET=tu_clave_secreta_de_al_menos_256_bits_para_HS256

# Perfil activo
SPRING_PROFILES_ACTIVE=dev

# Puerto del servidor
SERVER_PORT=3001
```

> En modo desarrollo (`dev`), la aplicacion usa H2 en memoria y no requiere configuracion adicional.

---

## Contribuir

Las contribuciones son bienvenidas. Para cambios grandes, abre primero un issue para discutir la propuesta.

1. Fork el repositorio
2. Crea tu rama de feature (`git checkout -b feature/nueva-funcionalidad`)
3. Haz commit de tus cambios (`git commit -m 'feat: agregar nueva funcionalidad'`)
4. Push a la rama (`git push origin feature/nueva-funcionalidad`)
5. Abre un Pull Request

### Convenciones de Commits

Este proyecto usa [Conventional Commits](https://www.conventionalcommits.org/):

- `feat:` nueva funcionalidad
- `fix:` correccion de bug
- `docs:` cambios en documentacion
- `refactor:` refactorizacion de codigo
- `test:` agregar o modificar tests

---

## Licencia

Distribuido bajo la Licencia MIT. Ver [`LICENSE`](LICENSE) para mas informacion.

---

<div align="center">

Desarrollado por **Ronald**

Si este proyecto te resulta util, considera darle una estrella en GitHub.

</div>
