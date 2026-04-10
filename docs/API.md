# Documentacion Completa de la API

## Base URL

- **Desarrollo:** `http://localhost:3001`
- **Produccion:** `http://localhost:8080`
- **Swagger UI:** `{base_url}/swagger-ui.html`

## Autenticacion

Todos los endpoints excepto `/api/auth/**` requieren un token JWT en el header:

```
Authorization: Bearer <token>
```

---

## Endpoints de Autenticacion

### POST /api/auth/register

Registra un nuevo usuario y retorna un token JWT.

**Request:**

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

**Campos del request:**

| Campo | Tipo | Requerido | Validacion |
|-------|------|-----------|-----------|
| `name` | String | Si | 2-100 caracteres |
| `email` | String | Si | Formato email valido |
| `password` | String | Si | 8-100 caracteres |
| `currency` | String | No | Exactamente 3 caracteres (default: USD) |

**Respuesta exitosa (201 Created):**

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJyb25hbGRAZXhhbXBsZS5jb20iLCJpYXQiOjE3MDUzMTUyMDAsImV4cCI6MTcwNTQwMTYwMH0.abc123",
  "email": "ronald@example.com",
  "name": "Ronald"
}
```

**Errores posibles:**

| Codigo | Escenario |
|--------|----------|
| 400 | Validacion fallida (email invalido, password corto, etc.) |
| 409 | El email ya esta registrado |

---

### POST /api/auth/login

Autentica un usuario existente y retorna un token JWT.

**Request:**

```bash
curl -X POST http://localhost:3001/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "ronald@example.com",
    "password": "miPassword123"
  }'
```

**Campos del request:**

| Campo | Tipo | Requerido | Validacion |
|-------|------|-----------|-----------|
| `email` | String | Si | Formato email valido |
| `password` | String | Si | No vacio |

**Respuesta exitosa (200 OK):**

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "email": "ronald@example.com",
  "name": "Ronald"
}
```

**Errores posibles:**

| Codigo | Escenario |
|--------|----------|
| 401 | Credenciales invalidas |

---

## Endpoints de Gastos

### GET /api/expenses

Lista gastos del usuario autenticado con filtros opcionales y paginacion.

**Request:**

```bash
curl "http://localhost:3001/api/expenses?categoryId=1&startDate=2024-01-01&endDate=2024-12-31&minAmount=10&maxAmount=500&page=0&size=20&sort=expenseDate,desc" \
  -H "Authorization: Bearer <token>"
```

**Parametros de query:**

| Parametro | Tipo | Requerido | Descripcion |
|-----------|------|-----------|------------|
| `categoryId` | Long | No | Filtrar por categoria |
| `startDate` | Date (YYYY-MM-DD) | No | Fecha inicio del rango |
| `endDate` | Date (YYYY-MM-DD) | No | Fecha fin del rango |
| `minAmount` | BigDecimal | No | Monto minimo |
| `maxAmount` | BigDecimal | No | Monto maximo |
| `page` | int | No | Pagina (default: 0) |
| `size` | int | No | Tamano de pagina (default: 20) |
| `sort` | String | No | Campo y direccion (default: expenseDate,desc) |

**Respuesta exitosa (200 OK):**

```json
{
  "content": [
    {
      "id": 1,
      "categoryId": 1,
      "categoryName": "Food & Dining",
      "categoryIcon": "utensils",
      "amount": 45.00,
      "description": "Almuerzo de trabajo",
      "expenseDate": "2024-01-15",
      "receiptUrl": null,
      "createdAt": "2024-01-15T10:30:00Z",
      "updatedAt": null
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "size": 20,
  "number": 0,
  "first": true,
  "last": true
}
```

---

### GET /api/expenses/{id}

Obtiene un gasto especifico por su ID.

**Request:**

```bash
curl http://localhost:3001/api/expenses/1 \
  -H "Authorization: Bearer <token>"
```

**Respuesta exitosa (200 OK):**

```json
{
  "id": 1,
  "categoryId": 1,
  "categoryName": "Food & Dining",
  "categoryIcon": "utensils",
  "amount": 45.00,
  "description": "Almuerzo de trabajo",
  "expenseDate": "2024-01-15",
  "receiptUrl": null,
  "createdAt": "2024-01-15T10:30:00Z",
  "updatedAt": null
}
```

**Errores posibles:**

| Codigo | Escenario |
|--------|----------|
| 404 | El gasto no existe o pertenece a otro usuario |

---

### POST /api/expenses

Crea un nuevo gasto.

**Request:**

```bash
curl -X POST http://localhost:3001/api/expenses \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "categoryId": 1,
    "amount": 45.00,
    "description": "Almuerzo de trabajo",
    "expenseDate": "2024-01-15",
    "receiptUrl": null
  }'
```

**Campos del request:**

| Campo | Tipo | Requerido | Validacion |
|-------|------|-----------|-----------|
| `categoryId` | Long | Si | Debe existir y pertenecer al usuario o ser default |
| `amount` | BigDecimal | Si | Mayor a 0.01, maximo 10 digitos enteros y 2 decimales |
| `description` | String | No | Maximo 500 caracteres |
| `expenseDate` | Date (YYYY-MM-DD) | Si | Fecha valida |
| `receiptUrl` | String | No | URL del recibo |

**Respuesta exitosa (201 Created):**

```json
{
  "id": 2,
  "categoryId": 1,
  "categoryName": "Food & Dining",
  "categoryIcon": "utensils",
  "amount": 45.00,
  "description": "Almuerzo de trabajo",
  "expenseDate": "2024-01-15",
  "receiptUrl": null,
  "createdAt": "2024-01-15T10:30:00Z",
  "updatedAt": null
}
```

**Errores posibles:**

| Codigo | Escenario |
|--------|----------|
| 400 | Validacion fallida (monto negativo, fecha nula, etc.) |
| 404 | La categoria no existe |

---

### PUT /api/expenses/{id}

Actualiza un gasto existente.

**Request:**

```bash
curl -X PUT http://localhost:3001/api/expenses/1 \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "categoryId": 2,
    "amount": 50.00,
    "description": "Almuerzo actualizado",
    "expenseDate": "2024-01-15",
    "receiptUrl": null
  }'
```

**Respuesta exitosa (200 OK):** Mismo formato que POST.

**Errores posibles:**

| Codigo | Escenario |
|--------|----------|
| 400 | Validacion fallida |
| 404 | Gasto o categoria no encontrada |

---

### DELETE /api/expenses/{id}

Elimina un gasto.

**Request:**

```bash
curl -X DELETE http://localhost:3001/api/expenses/1 \
  -H "Authorization: Bearer <token>"
```

**Respuesta exitosa:** `204 No Content`

**Errores posibles:**

| Codigo | Escenario |
|--------|----------|
| 404 | El gasto no existe o pertenece a otro usuario |

---

## Endpoints de Categorias

### GET /api/categories

Lista todas las categorias disponibles (predeterminadas + personalizadas del usuario).

**Request:**

```bash
curl http://localhost:3001/api/categories \
  -H "Authorization: Bearer <token>"
```

**Respuesta exitosa (200 OK):**

```json
[
  {
    "id": 1,
    "name": "Food & Dining",
    "icon": "utensils",
    "isDefault": true,
    "createdAt": "2024-01-01T00:00:00Z"
  },
  {
    "id": 15,
    "name": "Suscripciones",
    "icon": "credit-card",
    "isDefault": false,
    "createdAt": "2024-01-15T10:30:00Z"
  }
]
```

---

### GET /api/categories/{id}

Obtiene una categoria especifica.

```bash
curl http://localhost:3001/api/categories/1 \
  -H "Authorization: Bearer <token>"
```

---

### POST /api/categories

Crea una categoria personalizada.

```bash
curl -X POST http://localhost:3001/api/categories \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Suscripciones",
    "icon": "credit-card"
  }'
```

**Campos del request:**

| Campo | Tipo | Requerido | Validacion |
|-------|------|-----------|-----------|
| `name` | String | Si | 2-50 caracteres, unico por usuario |
| `icon` | String | No | Maximo 50 caracteres |

**Errores posibles:**

| Codigo | Escenario |
|--------|----------|
| 400 | Nombre invalido |
| 409 | Ya existe una categoria con ese nombre para el usuario |

---

### PUT /api/categories/{id}

Actualiza una categoria personalizada. Las categorias predeterminadas no se pueden modificar.

```bash
curl -X PUT http://localhost:3001/api/categories/15 \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Servicios Digitales",
    "icon": "globe"
  }'
```

**Errores posibles:**

| Codigo | Escenario |
|--------|----------|
| 400 | Intento de modificar una categoria predeterminada |
| 404 | Categoria no encontrada |

---

### DELETE /api/categories/{id}

Elimina una categoria personalizada. Las categorias predeterminadas no se pueden eliminar.

```bash
curl -X DELETE http://localhost:3001/api/categories/15 \
  -H "Authorization: Bearer <token>"
```

**Respuesta exitosa:** `204 No Content`

---

## Endpoints de Dashboard

### GET /api/dashboard/summary

Retorna un resumen financiero para el periodo especificado.

```bash
curl "http://localhost:3001/api/dashboard/summary?startDate=2024-01-01&endDate=2024-12-31" \
  -H "Authorization: Bearer <token>"
```

**Parametros requeridos:**

| Parametro | Tipo | Descripcion |
|-----------|------|------------|
| `startDate` | Date (YYYY-MM-DD) | Inicio del periodo |
| `endDate` | Date (YYYY-MM-DD) | Fin del periodo |

**Respuesta (200 OK):**

```json
{
  "totalExpenses": 1250.00,
  "transactionCount": 28,
  "averageExpense": 44.64,
  "highestExpense": 200.00
}
```

---

### GET /api/dashboard/by-category

Retorna el desglose de gastos por categoria con porcentajes.

```bash
curl "http://localhost:3001/api/dashboard/by-category?startDate=2024-01-01&endDate=2024-12-31" \
  -H "Authorization: Bearer <token>"
```

**Respuesta (200 OK):**

```json
[
  {
    "categoryId": 1,
    "categoryName": "Food & Dining",
    "categoryIcon": "utensils",
    "totalAmount": 450.00,
    "transactionCount": 12,
    "percentage": 36.00
  },
  {
    "categoryId": 2,
    "categoryName": "Transportation",
    "categoryIcon": "car",
    "totalAmount": 300.00,
    "transactionCount": 8,
    "percentage": 24.00
  }
]
```

---

### GET /api/dashboard/monthly-trend

Retorna la tendencia mensual de gastos.

```bash
curl "http://localhost:3001/api/dashboard/monthly-trend?startDate=2024-01-01&endDate=2024-12-31" \
  -H "Authorization: Bearer <token>"
```

**Respuesta (200 OK):**

```json
[
  {
    "year": 2024,
    "month": 1,
    "totalAmount": 350.00,
    "transactionCount": 8
  },
  {
    "year": 2024,
    "month": 2,
    "totalAmount": 420.00,
    "transactionCount": 10
  }
]
```

---

## Endpoints de Exportacion

### GET /api/export/csv

Descarga los gastos del periodo en formato CSV.

```bash
curl "http://localhost:3001/api/export/csv?startDate=2024-01-01&endDate=2024-12-31" \
  -H "Authorization: Bearer <token>" \
  -o expenses.csv
```

**Formato del CSV:**

```csv
Date,Category,Amount,Description
2024-01-15,"Food & Dining",45.00,"Almuerzo de trabajo"
2024-01-16,"Transportation",15.50,"Uber al centro"
```

---

### GET /api/export/pdf

Descarga los gastos del periodo en formato PDF con tabla estilizada.

```bash
curl "http://localhost:3001/api/export/pdf?startDate=2024-01-01&endDate=2024-12-31" \
  -H "Authorization: Bearer <token>" \
  -o expenses.pdf
```

El PDF incluye:
- Titulo "Expense Report"
- Subtitulo con el periodo seleccionado
- Tabla con columnas: Date, Category, Amount, Description
- Header con fondo oscuro y texto blanco

---

## Formato de Errores

Todas las respuestas de error siguen un formato consistente:

```json
{
  "status": 400,
  "message": "Validation failed",
  "errors": {
    "email": "Invalid email format",
    "password": "Password must be between 8 and 100 characters"
  },
  "timestamp": "2024-01-15T10:30:00Z"
}
```

El campo `errors` solo esta presente en errores de validacion (400). Para otros codigos de error, solo se incluye `status`, `message` y `timestamp`.

---

## Categorias Predeterminadas

El sistema incluye 14 categorias predeterminadas disponibles para todos los usuarios:

| ID | Nombre | Icono |
|----|--------|-------|
| 1 | Food & Dining | utensils |
| 2 | Transportation | car |
| 3 | Housing | home |
| 4 | Utilities | zap |
| 5 | Healthcare | heart-pulse |
| 6 | Entertainment | film |
| 7 | Shopping | shopping-bag |
| 8 | Education | book-open |
| 9 | Travel | plane |
| 10 | Software & Tools | laptop |
| 11 | Office Supplies | paperclip |
| 12 | Insurance | shield |
| 13 | Taxes | landmark |
| 14 | Other | more-horizontal |
