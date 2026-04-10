# API Reference

Base URL: `http://localhost:3001/api`

Todos los endpoints (excepto auth) requieren: `Authorization: Bearer <token>`

## Autenticacion

### POST /api/auth/register

| Campo | Tipo | Requerido | Validacion |
|-------|------|-----------|-----------|
| name | String | Si | 2-100 caracteres |
| email | String | Si | Email valido |
| password | String | Si | 8-100 caracteres |
| currency | String | No | 3 caracteres (default: USD) |

Retorna: `{ token, email, name }` - 201 Created

### POST /api/auth/login

| Campo | Tipo | Requerido |
|-------|------|-----------|
| email | String | Si |
| password | String | Si |

Retorna: `{ token, email, name }` - 200 OK

---

## Gastos

### GET /api/expenses

Parametros de query opcionales: `categoryId`, `startDate`, `endDate`, `minAmount`, `maxAmount`, `page`, `size`, `sort`

Retorna: `Page<ExpenseResponse>` - 200 OK

### GET /api/expenses/{id}

Retorna: `ExpenseResponse` - 200 OK

### POST /api/expenses

| Campo | Tipo | Requerido | Validacion |
|-------|------|-----------|-----------|
| categoryId | Long | Si | Debe existir |
| amount | BigDecimal | Si | > 0.01, max 10 enteros + 2 decimales |
| description | String | No | Max 500 caracteres |
| expenseDate | Date | Si | YYYY-MM-DD |
| receiptUrl | String | No | URL |

Retorna: `ExpenseResponse` - 201 Created

### PUT /api/expenses/{id}

Mismos campos que POST. Retorna: `ExpenseResponse` - 200 OK

### DELETE /api/expenses/{id}

Retorna: 204 No Content

---

## Categorias

### GET /api/categories

Retorna: `List<CategoryResponse>` con default + personalizadas - 200 OK

### GET /api/categories/{id}

Retorna: `CategoryResponse` - 200 OK

### POST /api/categories

| Campo | Tipo | Requerido | Validacion |
|-------|------|-----------|-----------|
| name | String | Si | 2-50 caracteres, unico por usuario |
| icon | String | No | Max 50 caracteres |

Retorna: `CategoryResponse` - 201 Created

### PUT /api/categories/{id}

Solo categorias personalizadas. Retorna: `CategoryResponse` - 200 OK

### DELETE /api/categories/{id}

Solo categorias personalizadas. Retorna: 204 No Content

---

## Dashboard

Parametros requeridos en todos: `startDate`, `endDate` (YYYY-MM-DD)

### GET /api/dashboard/summary

Retorna: `{ totalExpenses, transactionCount, averageExpense, highestExpense }`

### GET /api/dashboard/by-category

Retorna: `List<{ categoryId, categoryName, categoryIcon, totalAmount, transactionCount, percentage }>`

### GET /api/dashboard/monthly-trend

Retorna: `List<{ year, month, totalAmount, transactionCount }>`

---

## Exportacion

Parametros requeridos en todos: `startDate`, `endDate` (YYYY-MM-DD)

### GET /api/export/csv

Retorna: archivo CSV (Content-Type: text/csv)

### GET /api/export/pdf

Retorna: archivo PDF (Content-Type: application/pdf)

---

## Codigos de Error

| Codigo | Significado |
|--------|------------|
| 400 | Validacion fallida |
| 401 | Credenciales invalidas / token expirado |
| 403 | Sin token de autenticacion |
| 404 | Recurso no encontrado |
| 409 | Recurso duplicado |
| 500 | Error interno |

Formato de error:
```json
{
  "status": 400,
  "message": "Validation failed",
  "errors": { "campo": "mensaje" },
  "timestamp": "2024-01-15T10:30:00Z"
}
```
