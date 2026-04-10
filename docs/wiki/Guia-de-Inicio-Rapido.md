# Guia de Inicio Rapido

Configura ExpenseWise en tu maquina local en 5 minutos.

## Prerequisitos

- Java 21 (JDK)
- Maven 3.9+
- Git

## Paso 1: Clonar

```bash
git clone https://github.com/ronalc90/expense-wise.git
cd expense-wise
```

## Paso 2: Ejecutar

```bash
mvn spring-boot:run
```

La aplicacion arranca en modo desarrollo con H2 in-memory. No necesitas configurar base de datos.

## Paso 3: Explorar

| URL | Descripcion |
|-----|------------|
| http://localhost:3001/ | Frontend con glassmorphism UI |
| http://localhost:3001/swagger-ui.html | Documentacion interactiva de la API |
| http://localhost:3001/h2-console | Consola de base de datos (usuario: `sa`, sin password) |

## Paso 4: Probar la API

### Registrar un usuario

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

Guarda el `token` de la respuesta.

### Crear un gasto

```bash
curl -X POST http://localhost:3001/api/expenses \
  -H "Authorization: Bearer TU_TOKEN_AQUI" \
  -H "Content-Type: application/json" \
  -d '{
    "categoryId": 1,
    "amount": 25.50,
    "description": "Almuerzo",
    "expenseDate": "2024-01-15"
  }'
```

### Ver el dashboard

```bash
curl "http://localhost:3001/api/dashboard/summary?startDate=2024-01-01&endDate=2024-12-31" \
  -H "Authorization: Bearer TU_TOKEN_AQUI"
```

## Paso 5: Ejecutar Tests

```bash
mvn clean verify
```

Los 39 tests deben pasar exitosamente con H2 in-memory.

## Siguiente

- [Arquitectura](Arquitectura) - Entender el diseno del sistema
- [API Reference](API-Reference) - Ver todos los endpoints disponibles
- [Autenticacion](Autenticacion) - Entender el flujo JWT
