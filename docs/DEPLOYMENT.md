# Guia de Despliegue

## Contenido

1. [Desarrollo Local](#desarrollo-local)
2. [Docker](#docker)
3. [Produccion con PostgreSQL](#produccion-con-postgresql)
4. [Railway](#railway)
5. [Render](#render)
6. [Variables de Entorno](#variables-de-entorno)
7. [Consideraciones de Seguridad](#consideraciones-de-seguridad)

---

## Desarrollo Local

### Modo H2 (sin configuracion)

El perfil `dev` se activa por defecto y utiliza H2 in-memory:

```bash
# Compilar y ejecutar
mvn clean spring-boot:run

# O compilar primero y ejecutar el JAR
mvn clean package
java -jar target/expensewise-1.0.0-SNAPSHOT.jar
```

Endpoints disponibles:
- API: http://localhost:3001
- Swagger UI: http://localhost:3001/swagger-ui.html
- H2 Console: http://localhost:3001/h2-console (usuario: `sa`, sin password)
- Frontend: http://localhost:3001/

### Modo PostgreSQL local

```bash
# Iniciar PostgreSQL con Docker Compose
docker compose up -d postgres

# Ejecutar con perfil de produccion
SPRING_PROFILES_ACTIVE=prod \
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/expensewise \
SPRING_DATASOURCE_USERNAME=expensewise \
SPRING_DATASOURCE_PASSWORD=expensewise \
JWT_SECRET=mi-clave-secreta-de-al-menos-256-bits-para-HS256-algorithm \
mvn spring-boot:run
```

---

## Docker

### Construir la imagen

```bash
# Compilar primero (sin tests para velocidad)
mvn clean package -DskipTests

# Construir imagen Docker
docker build -t expensewise:latest .
```

### Ejecutar con Docker Compose

```bash
# Iniciar toda la infraestructura
docker compose up -d

# Verificar estado
docker compose ps

# Ver logs
docker compose logs -f

# Detener todo
docker compose down
```

### Ejecutar solo la aplicacion

```bash
docker run -d \
  --name expensewise \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/expensewise \
  -e SPRING_DATASOURCE_USERNAME=expensewise \
  -e SPRING_DATASOURCE_PASSWORD=password-seguro \
  -e JWT_SECRET=clave-secreta-256-bits \
  expensewise:latest
```

### Dockerfile

```dockerfile
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/expensewise-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

La imagen usa `eclipse-temurin:21-jre-alpine` para un tamano minimo (~200MB).

---

## Railway

### Despliegue automatico

1. Crear cuenta en [Railway](https://railway.app/)
2. Crear un nuevo proyecto
3. Agregar servicio PostgreSQL desde el marketplace
4. Conectar el repositorio de GitHub `ronalc90/expense-wise`
5. Configurar variables de entorno:

```
SPRING_PROFILES_ACTIVE=prod
SPRING_DATASOURCE_URL=${{Postgres.DATABASE_URL}}
JWT_SECRET=<generar-clave-segura>
```

6. Railway detectara automaticamente el Dockerfile o usara Nixpacks

### Comando de build personalizado

Si Railway no detecta la configuracion automaticamente:

- **Build command:** `mvn clean package -DskipTests`
- **Start command:** `java -jar target/expensewise-1.0.0-SNAPSHOT.jar`
- **Puerto:** `8080`

---

## Render

### Despliegue como Web Service

1. Crear cuenta en [Render](https://render.com/)
2. Crear un nuevo Web Service
3. Conectar el repositorio de GitHub
4. Configurar:
   - **Environment:** Docker
   - **Plan:** Free o Starter
5. Agregar una base de datos PostgreSQL
6. Configurar variables de entorno:

```
SPRING_PROFILES_ACTIVE=prod
SPRING_DATASOURCE_URL=<URL de Render PostgreSQL>
SPRING_DATASOURCE_USERNAME=<usuario>
SPRING_DATASOURCE_PASSWORD=<password>
JWT_SECRET=<generar-clave-segura>
```

---

## Variables de Entorno

| Variable | Requerida | Descripcion |
|----------|-----------|------------|
| `SPRING_PROFILES_ACTIVE` | Si | Usar `prod` en produccion |
| `SPRING_DATASOURCE_URL` | Si | URL JDBC de PostgreSQL |
| `SPRING_DATASOURCE_USERNAME` | Si | Usuario de la base de datos |
| `SPRING_DATASOURCE_PASSWORD` | Si | Contrasena de la base de datos |
| `JWT_SECRET` | Si | Clave HS256 (minimo 256 bits / 32 caracteres) |
| `SERVER_PORT` | No | Puerto del servidor (default: 8080) |

### Generar JWT_SECRET seguro

```bash
# Linux/macOS
openssl rand -base64 48

# O con /dev/urandom
head -c 48 /dev/urandom | base64
```

---

## Consideraciones de Seguridad

### En produccion, siempre:

1. **Cambiar el JWT_SECRET** - Nunca usar la clave por defecto
2. **Usar HTTPS** - Configurar un proxy inverso (Nginx, Caddy) o usar el TLS del proveedor cloud
3. **Restringir CORS** - Modificar `SecurityConfig.java` para permitir solo dominios de produccion
4. **Desactivar H2 Console** - El perfil `prod` no habilita la consola H2
5. **Rotar credenciales** - Cambiar periodicamente la contrasena de la base de datos y el JWT_SECRET
6. **Backups** - Configurar backups automaticos de PostgreSQL

### Ejemplo de Nginx como proxy inverso

```nginx
server {
    listen 80;
    server_name api.expensewise.com;
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl;
    server_name api.expensewise.com;

    ssl_certificate /etc/letsencrypt/live/api.expensewise.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/api.expensewise.com/privkey.pem;

    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```
