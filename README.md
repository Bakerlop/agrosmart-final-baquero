# AgroSmart – Plataforma Reactiva de Comercialización Agrícola

## Información del estudiante

- **Nombre:** Fabricio Fernando Baquero López
- **Cédula:** 1719271643
- **NN:** 43
- **Categoría asignada:** Café
- **Asignatura:** Programación Avanzada
- **Universidad:** ESPE

---

# Semilla personal

La semilla utilizada corresponde a los dos últimos dígitos de mi cédula (**43**).

Con base en esta semilla:

- Tabla utilizada: `tbl_productos_base_43`
- Categoría asignada: **Café**
- Puerto configurado: **8143**

---

# Tecnologías utilizadas

- Java 21
- Spring Boot 4
- Spring WebFlux
- Spring Data JPA
- Hibernate
- PostgreSQL 17
- LangChain4j
- Reactor
- Maven
- Docker

---

# Configuración de la base de datos

Levantar PostgreSQL:

```bash
docker compose up -d
```

Verificar el contenedor:

```bash
docker ps
```

Comprobar la conexión:

```bash
docker exec agrosmart-postgres-clean pg_isready -U agrosmart -d agrosmart_db
```

---

# Variables de entorno

El proyecto utiliza el perfil:

```properties
spring.profiles.active=prod
```

La configuración principal se encuentra en:

```
src/main/resources/application-prod.properties
```

---

# Ejecutar la aplicación

Windows

```bash
.\mvnw.cmd spring-boot:run
```

Linux / macOS

```bash
./mvnw spring-boot:run
```

La aplicación inicia en:

```
http://localhost:8143
```

---

# Endpoints

## Obtener productos comercializables

```bash
curl http://localhost:8143/api/productos
```

Respuesta:

```json
[
  {
    "id":1,
    "nombre":"CAFÉ ARÁBIGO DE ALTURA",
    "categoria":"Café",
    "precioUsd":8.50
  },
  {
    "id":2,
    "nombre":"CAFÉ ORGÁNICO LAVADO",
    "categoria":"Café",
    "precioUsd":10.75
  },
  {
    "id":3,
    "nombre":"CAFÉ ROBUSTA TOSTADO",
    "categoria":"Café",
    "precioUsd":7.25
  }
]
```

---

## Buscar producto por id

```bash
curl http://localhost:8143/api/productos/1
```

Respuesta:

```json
{
  "id":1,
  "nombre":"Café arábigo de altura",
  "categoria":"Café",
  "precioUsd":8.50
}
```

---

## Producto inexistente

```bash
curl -i http://localhost:8143/api/productos/9999
```

Respuesta:

```
HTTP/1.1 404 Not Found
```

---

## Generar publicidad con IA

```bash
curl "http://localhost:8143/api/agrosmart/publicidad?producto=Cacao%20fino%20de%20aroma&audiencia=exportadores%20europeos"
```

Respuesta obtenida durante la prueba:

```
Publicidad no disponible en este momento (AuthenticationException)
```

---

# Operadores reactivos utilizados

## Mono.fromCallable()

Envuelve las operaciones bloqueantes de JPA y LangChain4j para diferir su ejecución hasta que exista una suscripción.

## subscribeOn(Schedulers.boundedElastic())

Ejecuta las operaciones bloqueantes fuera del Event Loop de Netty.

## flatMapMany()

Convierte la lista obtenida desde JPA en un Flux.

## map()

Transforma ProductoEntity en Producto y convierte el nombre a mayúsculas.

## filter()

Mantiene únicamente los productos válidos.

## doOnNext()

Registra información del producto sin modificarlo.

## defaultIfEmpty()

Emite un producto genérico cuando el flujo queda vacío.

## switchIfEmpty()

Genera una excepción cuando el producto solicitado no existe.

## timeout()

Cancela la llamada a la IA si supera los 30 segundos.

## onErrorResume()

Convierte cualquier error del proveedor de IA en un mensaje controlado para el usuario.

---

# Puente bloqueante → Reactivo

ProductoRepository utiliza Hibernate y PostgreSQL, que realizan operaciones bloqueantes.

Para integrarlas con WebFlux se utilizó:

```java
Mono.fromCallable(...)
.subscribeOn(Schedulers.boundedElastic())
```

Esto permite ejecutar las consultas bloqueantes en un pool de hilos independiente sin bloquear el Event Loop de Netty.

La misma estrategia se utilizó para las llamadas realizadas por LangChain4j al proveedor de inteligencia artificial.

---

# Pruebas unitarias

Se implementaron pruebas para:

- ProductoTest
- ProductoFiltersTest
- ProductoServiceTest
- PublicidadServiceTest

Resultado:

```
Tests run: 12
Failures: 0
Errors: 0
BUILD SUCCESS
```

---

# Evidencias

Las evidencias del examen se encuentran en:

```
docs/evidencias/
```

Incluyen:

- Arranque de la aplicación.
- Tabla PostgreSQL.
- Consultas realizadas con curl.
- Pruebas unitarias.
- Historial Git.

---

# Repositorio

https://github.com/Bakerlop/agrosmart-final-baquero