# Modelo de Datos

## Diagrama Entidad-Relacion

```
+------------------+       +-------------------+       +------------------+
|      users       |       |    categories     |       |     expenses     |
+------------------+       +-------------------+       +------------------+
| id          PK   |<--+   | id          PK    |<--+   | id          PK   |
| email     UNIQUE |   +---| user_id    FK     |   +---| category_id FK   |
| password_hash    |       | name              |       | user_id     FK   |---+
| name             |       | icon              |       | amount           |   |
| currency (3)     |       | is_default BOOL   |       | description      |   |
| created_at       |       | created_at        |       | expense_date     |   |
+------------------+       | UNIQUE(name,      |       | receipt_url      |   |
                           |   user_id)        |       | created_at       |   |
                           +-------------------+       | updated_at       |   |
                                                       +------------------+   |
                                                              |               |
                                                              +---------------+
                                                              users.id
```

## Tabla: users

| Columna | Tipo | Nullable | Descripcion |
|---------|------|----------|------------|
| id | BIGSERIAL | PK | Identificador auto-incremental |
| email | VARCHAR(255) | NOT NULL, UNIQUE | Email del usuario |
| password_hash | VARCHAR(255) | NOT NULL | Hash BCrypt de la contrasena |
| name | VARCHAR(100) | NOT NULL | Nombre del usuario |
| currency | VARCHAR(3) | NOT NULL, DEFAULT 'USD' | Moneda preferida (ISO 4217) |
| created_at | TIMESTAMPTZ | NOT NULL, DEFAULT NOW() | Fecha de registro |

**Indices:** `idx_users_email ON users(email)`

## Tabla: categories

| Columna | Tipo | Nullable | Descripcion |
|---------|------|----------|------------|
| id | BIGSERIAL | PK | Identificador auto-incremental |
| name | VARCHAR(50) | NOT NULL | Nombre de la categoria |
| icon | VARCHAR(50) | NULL | Nombre del icono (Lucide icons) |
| user_id | BIGINT | NULL, FK -> users(id) | NULL = categoria predeterminada |
| is_default | BOOLEAN | NOT NULL, DEFAULT FALSE | Si es predeterminada |
| created_at | TIMESTAMPTZ | NOT NULL, DEFAULT NOW() | Fecha de creacion |

**Constraint:** `UNIQUE(name, user_id)` - Nombres unicos por usuario  
**Indice:** `idx_categories_user_id ON categories(user_id)`

### Categorias Predeterminadas (14)

| Nombre | Icono |
|--------|-------|
| Food & Dining | utensils |
| Transportation | car |
| Housing | home |
| Utilities | zap |
| Healthcare | heart-pulse |
| Entertainment | film |
| Shopping | shopping-bag |
| Education | book-open |
| Travel | plane |
| Software & Tools | laptop |
| Office Supplies | paperclip |
| Insurance | shield |
| Taxes | landmark |
| Other | more-horizontal |

## Tabla: expenses

| Columna | Tipo | Nullable | Descripcion |
|---------|------|----------|------------|
| id | BIGSERIAL | PK | Identificador auto-incremental |
| user_id | BIGINT | NOT NULL, FK -> users(id) | Propietario del gasto |
| category_id | BIGINT | NOT NULL, FK -> categories(id) | Categoria del gasto |
| amount | DECIMAL(12,2) | NOT NULL | Monto del gasto |
| description | VARCHAR(500) | NULL | Descripcion opcional |
| expense_date | DATE | NOT NULL | Fecha del gasto |
| receipt_url | VARCHAR(500) | NULL | URL del recibo |
| created_at | TIMESTAMPTZ | NOT NULL, DEFAULT NOW() | Fecha de creacion |
| updated_at | TIMESTAMPTZ | NULL | Ultima modificacion |

**Indices:**
- `idx_expenses_user_id ON expenses(user_id)`
- `idx_expenses_category_id ON expenses(category_id)`
- `idx_expenses_expense_date ON expenses(expense_date)`
- `idx_expenses_user_date ON expenses(user_id, expense_date)` -- Compuesto para dashboard

## Migraciones Flyway

| Version | Archivo | Descripcion |
|---------|---------|------------|
| V1 | `V1__create_users.sql` | Tabla users + indice email |
| V2 | `V2__create_categories.sql` | Tabla categories + constraint unique |
| V3 | `V3__create_expenses.sql` | Tabla expenses + 4 indices |
| V4 | `V4__seed_default_categories.sql` | INSERT de 14 categorias predeterminadas |

## Relaciones

- **User 1:N Expense** - Un usuario tiene muchos gastos (CASCADE en delete)
- **User 1:N Category** - Un usuario tiene muchas categorias personalizadas (CASCADE en delete)
- **Category 1:N Expense** - Una categoria tiene muchos gastos
- **Category sin user** = categoria predeterminada (disponible para todos)
