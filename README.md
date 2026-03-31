# DNI API - Consulta RENIEC con caché en MySQL

API REST construida con Spring Boot para consultar datos por DNI usando un proveedor RENIEC externo y guardar resultados en base de datos para evitar consumir tokens en consultas repetidas.

## Objetivo del proyecto

- Exponer un endpoint simple para consulta de DNI.
- Validar el formato del DNI.
- Consultar primero en base de datos local (caché).
- Si no existe en caché, consultar proveedor RENIEC.
- Persistir el resultado en MySQL para futuras consultas.
- Devolver errores HTTP claros para cada caso.

## Arquitectura

La aplicación sigue una arquitectura en capas:

- Capa de entrada (Controller): recibe requests HTTP.
- Capa de aplicación (Service): contiene la lógica de negocio.
- Capa de persistencia (Repository + Entity): acceso y almacenamiento en MySQL.
- Capa de integración externa (RestTemplate + DTO): consumo del proveedor RENIEC.
- Capa de manejo de errores global: transforma excepciones en respuestas HTTP consistentes.

### Flujo funcional (cache-first)

1. Llega `GET /api/dni/{dni}`.
2. Se valida que el DNI tenga exactamente 8 dígitos.
3. Se busca el DNI en tabla `personas`.
4. Si existe, se responde desde base de datos.
5. Si no existe, se consulta `reniec.api.url` con token Bearer.
6. Se normaliza nombre y apellidos.
7. Se guarda en `personas`.
8. Se responde al cliente.

## Estructura del proyecto

```text
src/main/java/com/consulta_dni/dni_api
├── config
│   └── RestTemplateConfig.java
├── controlador
│   └── PersonaControlador.java
├── dto
│   ├── PersonaRespuestaDTO.java
│   └── ReniecRespuestaDTO.java
├── excepcion
│   ├── ManejadorExcepcionesGlobal.java
│   └── RecursoNoEncontradoExcepcion.java
├── modelo
│   └── Persona.java
├── repositorio
│   └── PersonaRepositorio.java
├── servicio
│   ├── PersonaServicio.java
│   └── impl
│       └── PersonaServicioImpl.java
└── DniApiApplication.java
```

## Componentes clave

### 1) Controller

- `PersonaControlador` expone:
  - `GET /api/dni/{dni}`

### 2) Service

- `PersonaServicioImpl`:
  - Valida DNI.
  - Busca en caché (MySQL).
  - Consulta RENIEC si no existe en caché.
  - Mapea y normaliza datos.
  - Persiste resultados.

### 3) Persistencia

- `Persona` es entidad JPA mapeada a tabla `personas`.
- `PersonaRepositorio` hereda de `JpaRepository<Persona, String>`.

### 4) Integración RENIEC

- `RestTemplateConfig` configura timeouts.
- `ReniecRespuestaDTO` soporta múltiples alias de campos para compatibilidad de payload.

### 5) Manejo de errores

- `ManejadorExcepcionesGlobal` transforma excepciones a:
  - 400: DNI inválido.
  - 404: DNI no encontrado.
  - 502: error de token, proveedor externo, red o rate limit.

## Requisitos

- Java 21
- Maven (o usar `mvnw` del proyecto)
- MySQL 8+
- Acceso a token RENIEC del proveedor configurado

## Configuración

Archivo `src/main/resources/application.properties`:

```properties
server.port=8080

spring.datasource.url=jdbc:mysql://localhost:3306/dni_db
spring.datasource.username=root
spring.datasource.password=123456
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

reniec.api.url=https://api.decolecta.com/v1/reniec/dni
reniec.api.token=TU_TOKEN
reniec.api.timeout=5000
```

## Construcción y ejecución paso a paso

### Paso 1: crear base de datos

```sql
CREATE DATABASE IF NOT EXISTS dni_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### Paso 2: validar credenciales MySQL

Confirma que usuario y contraseña del `application.properties` tengan permisos sobre `dni_db`.

### Paso 3: compilar

Windows (PowerShell):

```bash
.\mvnw.cmd clean compile
```

### Paso 4: ejecutar tests

```bash
.\mvnw.cmd test
```

### Paso 5: iniciar la API

```bash
.\mvnw.cmd spring-boot:run
```

### Paso 6: probar endpoint

```http
GET http://localhost:8080/api/dni/70846414
```

## Pruebas con Postman

### Request principal

- Método: `GET`
- URL: `http://localhost:8080/api/dni/{dni}`
- Headers recomendados:
  - `Accept: application/json`

### Ejemplos

- `http://localhost:8080/api/dni/70846414`
- `http://localhost:8080/api/dni/12345678`
- `http://localhost:8080/api/dni/ABC` (debe responder 400)

## Respuestas esperadas

### 200 OK

```json
{
  "dni": "70846414",
  "nombreCompleto": "Nombre ApellidoPaterno ApellidoMaterno"
}
```

### 400 Bad Request

```json
{
  "error": "El DNI debe tener exactamente 8 dígitos numéricos"
}
```

### 404 Not Found

```json
{
  "error": "No se encontró persona con DNI: 00000000"
}
```

### 502 Bad Gateway

```json
{
  "error": "Token de autorización inválido o expirado. Verifica tu token del proveedor RENIEC"
}
```

## Solución de problemas frecuentes

### Error de MySQL: Access denied for user 'root'@'localhost'

Causa:
- Usuario o contraseña incorrectos.

Solución:
- Ajustar `spring.datasource.username` y `spring.datasource.password`.
- Verificar acceso manual en MySQL.

### Token inválido o expirado

Causa:
- Token RENIEC incorrecto, vencido o sin permisos.

Solución:
- Generar token nuevo en el proveedor.
- Actualizar `reniec.api.token`.

### Se supera límite de consultas

Causa:
- Rate limit del proveedor.

Solución:
- Esperar y reintentar.
- Aprovechar caché local (ya implementada).

## Mejoras recomendadas

- Mover credenciales y token a variables de entorno.
- Agregar auditoría de fecha de consulta y TTL de caché.
- Agregar pruebas unitarias y de integración automatizadas.
- Agregar logs estructurados por solicitud y proveedor.
