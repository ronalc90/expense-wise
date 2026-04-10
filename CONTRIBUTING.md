# Guia de Contribucion

Gracias por tu interes en contribuir a ExpenseWise. Esta guia te ayudara a entender el proceso.

## Como Contribuir

### 1. Fork y Clone

```bash
# Fork el repositorio en GitHub, luego:
git clone https://github.com/TU_USUARIO/expense-wise.git
cd expense-wise
git remote add upstream https://github.com/ronalc90/expense-wise.git
```

### 2. Crear una Rama

Usa la siguiente convencion de nombres:

| Prefijo | Uso | Ejemplo |
|---------|-----|---------|
| `feature/` | Nueva funcionalidad | `feature/multi-moneda` |
| `fix/` | Correccion de bug | `fix/validacion-monto` |
| `refactor/` | Refactorizacion | `refactor/extract-service` |
| `docs/` | Documentacion | `docs/api-examples` |
| `test/` | Tests nuevos | `test/export-service` |

```bash
git checkout -b feature/mi-nueva-funcionalidad
```

### 3. Hacer Cambios

- Sigue los principios SOLID
- Mantiene la arquitectura de capas (Controller -> Service -> Repository)
- Sin logica de negocio en controllers
- Usa records para DTOs nuevos
- Agrega validaciones con Bean Validation
- Operaciones de escritura deben ser `@Transactional`

### 4. Ejecutar Tests

```bash
# Todos los tests
mvn clean verify

# Solo tests unitarios
mvn test -Dtest="*ServiceTest"

# Solo tests de integracion
mvn test -Dtest="*ControllerTest"
```

Los tests deben pasar antes de crear el PR.

### 5. Commit

Usamos [Conventional Commits](https://www.conventionalcommits.org/):

```
<tipo>: <descripcion>
```

**Tipos permitidos:**

| Tipo | Descripcion |
|------|------------|
| `feat` | Nueva funcionalidad |
| `fix` | Correccion de bug |
| `docs` | Solo documentacion |
| `refactor` | Refactorizacion sin cambio de funcionalidad |
| `test` | Agregar o modificar tests |
| `chore` | Tareas de mantenimiento (build, deps, etc.) |
| `style` | Formato, espacios, punto y coma (sin cambio de logica) |
| `perf` | Mejora de rendimiento |

**Ejemplos:**

```bash
git commit -m "feat: agregar soporte multi-moneda"
git commit -m "fix: corregir calculo de promedio con cero gastos"
git commit -m "test: agregar tests para ExportService"
git commit -m "docs: documentar endpoints de exportacion"
```

### 6. Push y Pull Request

```bash
git push origin feature/mi-nueva-funcionalidad
```

Luego crea un Pull Request en GitHub con:

- Titulo descriptivo siguiendo Conventional Commits
- Descripcion de los cambios realizados
- Referencia a issues relacionados (si aplica)
- Capturas de pantalla (si hay cambios visuales)

---

## Estructura del Codigo

```
src/main/java/com/expensewise/
├── config/         # Configuracion de Spring (Security, JWT, Beans)
├── controller/     # REST Controllers (solo delegacion)
├── service/        # Logica de negocio
├── domain/
│   ├── entity/     # Entidades JPA
│   └── repository/ # Spring Data Repositories
├── dto/            # Java Records para request/response
├── security/       # JWT filter, provider, user context
└── exception/      # Excepciones y handler global
```

## Estandares de Codigo

- **Inyeccion por constructor** (nunca `@Autowired` en campos)
- **Records para DTOs** (inmutables, con validaciones)
- **Entidades JPA con Lombok** (Builder, Getter, Setter)
- **Nombres en ingles** para codigo, comentarios en espanol donde aplique
- **Tests**: caso feliz + validaciones + reglas de negocio
- **Sin sobreingenieria**: no agregar abstracciones innecesarias

## Reporte de Bugs

Abre un [Issue](https://github.com/ronalc90/expense-wise/issues) con:

1. Descripcion del bug
2. Pasos para reproducir
3. Comportamiento esperado vs. actual
4. Version de Java y Spring Boot
5. Logs relevantes

## Solicitud de Features

Abre un [Issue](https://github.com/ronalc90/expense-wise/issues) con el label `enhancement` describiendo:

1. Que funcionalidad necesitas
2. Por que es importante
3. Propuesta de implementacion (opcional)
