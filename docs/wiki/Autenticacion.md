# Autenticacion

ExpenseWise utiliza JWT (JSON Web Tokens) para autenticacion stateless.

## Flujo de Autenticacion

### 1. Registro

```
Cliente --> POST /api/auth/register
            { name, email, password, currency }
                    |
                    v
            AuthController
                    |
                    v
            AuthService.register()
              1. Verifica que el email no exista (409 si duplicado)
              2. Hashea la contrasena con BCrypt
              3. Guarda el usuario en la BD
              4. Genera un token JWT
                    |
                    v
            Respuesta: { token, email, name }
```

### 2. Login

```
Cliente --> POST /api/auth/login
            { email, password }
                    |
                    v
            AuthController
                    |
                    v
            AuthService.login()
              1. AuthenticationManager valida credenciales
              2. Si falla: 401 Unauthorized
              3. Carga el usuario de la BD
              4. Genera un token JWT
                    |
                    v
            Respuesta: { token, email, name }
```

### 3. Request Autenticado

```
Cliente --> GET /api/expenses
            Header: Authorization: Bearer <token>
                    |
                    v
            JwtAuthenticationFilter (OncePerRequestFilter)
              1. Extrae el token del header "Authorization"
              2. Valida el token con JwtTokenProvider
              3. Extrae el email del token (subject)
              4. Carga UserDetails via CustomUserDetailsService
              5. Establece el SecurityContext
                    |
                    v
            ExpenseController (acceso permitido)
              -> ExpenseService
                -> SecurityUserContext.getCurrentUser()
                   (obtiene el usuario del SecurityContext)
```

## Configuracion JWT

### application.yml

```yaml
app:
  jwt:
    secret: ${JWT_SECRET:claveDefaultParaDesarrollo}
    expiration-ms: 86400000  # 24 horas
```

### JwtProperties (Record inmutable)

```java
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
    String secret,
    long expirationMs
) {}
```

## Componentes de Seguridad

### JwtTokenProvider

- Genera tokens firmados con HMAC-SHA256
- Valida tokens y extrae el subject (email)
- Recibe `Clock` inyectable para testing determinista

### JwtAuthenticationFilter

- Extiende `OncePerRequestFilter`
- Se ejecuta antes de `UsernamePasswordAuthenticationFilter`
- Extrae el token del header `Authorization: Bearer <token>`
- Si el token es valido, establece la autenticacion en el SecurityContext

### SecurityConfig

- CSRF desactivado (API REST stateless)
- Sesiones: `STATELESS`
- Endpoints publicos: `/api/auth/**`, `/swagger-ui/**`, `/h2-console/**`, recursos estaticos
- Todo lo demas requiere autenticacion
- CORS configurado para localhost:3000 y localhost:5173
- BCryptPasswordEncoder para hashing de contrasenas

### SecurityUserContext

- Componente que abstrae el acceso al usuario autenticado
- `getCurrentUser()` retorna la entidad `User` completa
- `getCurrentUserId()` retorna solo el ID
- Lanza `UnauthorizedException` si no hay autenticacion

## Token JWT

### Estructura

```
Header:  { "alg": "HS256" }
Payload: { "sub": "email@example.com", "iat": 1705315200, "exp": 1705401600 }
```

### Uso en requests

```bash
curl -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  http://localhost:3001/api/expenses
```

## Seguridad en Produccion

1. **Cambiar JWT_SECRET**: Usar una clave de al menos 256 bits generada aleatoriamente
2. **HTTPS**: Configurar TLS para proteger los tokens en transito
3. **Rotar secretos**: Cambiar periodicamente el JWT_SECRET
4. **CORS restrictivo**: Limitar a dominios de produccion especificos
