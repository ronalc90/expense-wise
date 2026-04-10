# Testing

## Resumen

ExpenseWise tiene **39 tests** organizados en dos niveles:

- **12 tests unitarios** en `service/`
- **27 tests de integracion** en `controller/`

## Ejecutar Tests

### Todos los tests

```bash
mvn clean verify
```

### Solo tests unitarios

```bash
mvn test -Dtest="*ServiceTest"
```

### Solo tests de integracion

```bash
mvn test -Dtest="*ControllerTest"
```

### Un test especifico

```bash
mvn test -Dtest="ExpenseControllerTest#createExpense_Success"
```

## Estrategia de Testing

### Tests Unitarios (Mockito)

Ubicacion: `src/test/java/com/expensewise/service/`

Se usa `@ExtendWith(MockitoExtension.class)` con `@Mock` e `@InjectMocks` para aislar completamente el servicio de sus dependencias.

**Cobertura:**
- Caso feliz (operacion exitosa)
- Errores de validacion (recurso no encontrado, duplicados)
- Reglas de negocio (moneda por defecto, etc.)
- Verificacion de interacciones con repositories

**Ejemplo:**

```java
@Test
@DisplayName("Should throw exception when category not found")
void createExpense_CategoryNotFound() {
    when(categoryRepository.findByIdAndUserIdOrDefault(999L, 1L))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> expenseService.createExpense(request))
        .isInstanceOf(ResourceNotFoundException.class);
}
```

### Tests de Integracion (MockMvc + H2)

Ubicacion: `src/test/java/com/expensewise/controller/`

Se usa `@SpringBootTest` con `@AutoConfigureMockMvc` para levantar el contexto completo de Spring. La base de datos es H2 in-memory con el perfil `test`.

**Flujo testeado:** HTTP request -> Controller -> Service -> Repository -> H2

**Clase base:** `BaseIntegrationTest`
- Configura MockMvc, ObjectMapper y repositories
- Crea un usuario de prueba con contrasena hasheada
- Genera un token JWT valido para cada test
- Limpia la base de datos antes de cada test (`@BeforeEach`)

**Cobertura:**
- Flujo completo de request/response
- Autenticacion JWT real
- Codigos de estado HTTP correctos
- Formato de respuesta JSON
- Validaciones de request
- Casos de error (404, 401, 409, 400)

## Desglose por Clase

| Clase | Tests | Tipo | Escenarios |
|-------|-------|------|-----------|
| **AuthControllerTest** | 7 | Integracion | Registro exitoso, email duplicado, email invalido, password corto, login exitoso, password incorrecto, email inexistente |
| **ExpenseControllerTest** | 8 | Integracion | Crear, obtener por ID, listar con paginacion, actualizar, eliminar, sin auth (403), monto invalido, ID inexistente |
| **CategoryControllerTest** | 8 | Integracion | Listar con defaults, obtener por ID, crear, duplicado (409), actualizar, default no editable, eliminar, sin auth |
| **DashboardControllerTest** | 4 | Integracion | Summary, breakdown por categoria, tendencia mensual, rango vacio |
| **ExpenseServiceTest** | 7 | Unitario | Crear, categoria no encontrada, obtener por ID, ID no encontrado, actualizar, eliminar, eliminar no encontrado |
| **AuthServiceTest** | 5 | Unitario | Registro, email duplicado, moneda default, login, credenciales invalidas |

## Configuracion de Tests

### application-test.yml

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL
    driver-class-name: org.h2.Driver
  jpa:
    hibernate:
      ddl-auto: create-drop
  flyway:
    enabled: false
app:
  jwt:
    secret: testSecretKeyThatIsAtLeast256BitsLong...
    expiration-ms: 3600000
```

### Maven Surefire

```xml
<plugin>
    <artifactId>maven-surefire-plugin</artifactId>
    <configuration>
        <argLine>
            -XX:+EnableDynamicAgentLoading
            --add-opens java.base/java.lang=ALL-UNNAMED
        </argLine>
    </configuration>
</plugin>
```

Los flags `--add-opens` son necesarios para Mockito con Java 21.
