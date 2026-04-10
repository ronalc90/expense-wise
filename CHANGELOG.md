# Changelog

Todos los cambios notables del proyecto se documentan en este archivo.

El formato esta basado en [Keep a Changelog](https://keepachangelog.com/es-ES/1.1.0/),
y el proyecto sigue [Versionado Semantico](https://semver.org/lang/es/).

---

## [1.0.0] - 2026-04-10

### Agregado

#### Autenticacion
- Registro de usuarios con validacion completa (nombre, email, password, moneda)
- Login con generacion de token JWT (HS256, 24h de expiracion)
- Hashing seguro de contrasenas con BCrypt
- Filtro de autenticacion JWT en cadena de Spring Security
- Endpoints publicos para auth, Swagger UI y recursos estaticos

#### Gestion de Gastos
- CRUD completo de gastos (crear, leer, actualizar, eliminar)
- Filtrado avanzado por categoria, rango de fechas y rango de montos
- Paginacion server-side con ordenamiento configurable
- Aislamiento de datos por usuario (multi-tenant a nivel de fila)
- Validacion declarativa con Bean Validation (monto > 0, fecha requerida, etc.)

#### Categorias
- 14 categorias predeterminadas con iconos (Food & Dining, Transportation, Housing, etc.)
- Creacion de categorias personalizadas por usuario
- Proteccion de categorias predeterminadas (no editables ni eliminables)
- Validacion de unicidad por nombre dentro del mismo usuario

#### Dashboard Analitico
- Resumen financiero: total de gastos, conteo de transacciones, promedio y gasto maximo
- Desglose por categoria con porcentajes relativos
- Tendencia mensual con agrupacion por ano y mes
- Filtrado por rango de fechas en todas las consultas

#### Exportacion
- Exportacion a CSV con formato compatible con Excel
- Exportacion a PDF con tabla profesional estilizada (iTextPDF)
- Ambos formatos filtran por rango de fechas

#### Frontend
- Interfaz web con efecto glassmorphism oscuro
- Single Page Application servida como recurso estatico de Spring Boot
- Responsive design para desktop y movil
- Graficos interactivos para el dashboard

#### Infraestructura
- Perfiles de configuracion: dev (H2), test (H2), prod (PostgreSQL)
- Migraciones Flyway versionadas (V1-V4)
- Dockerfile con eclipse-temurin:21-jre-alpine
- Docker Compose con PostgreSQL 16 y Redis 7
- Pipeline CI/CD con GitHub Actions
- Documentacion Swagger/OpenAPI automatica

#### Testing
- 39 tests totales (27 integracion + 12 unitarios)
- Tests de integracion con MockMvc, H2 y JWT real
- Tests unitarios con Mockito
- Clase base `BaseIntegrationTest` para setup comun

#### Documentacion
- README completo con badges, arquitectura y guia de uso
- Documentacion de arquitectura (docs/ARCHITECTURE.md)
- Documentacion completa de API con ejemplos curl (docs/API.md)
- Architecture Decision Records (docs/DECISIONS.md)
- Guia de despliegue (docs/DEPLOYMENT.md)
- Guia de contribucion (CONTRIBUTING.md)
- Licencia MIT
