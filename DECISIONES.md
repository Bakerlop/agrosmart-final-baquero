# 🧭 DECISIONES.md — Bitácora de diseño

> **Instrucciones.** Completa **una entrada por fase**, en **primera persona** y
> **refiriéndote a tu propio código**: nombres reales de tus clases, tu tabla, tus
> líneas, tu salida real de terminal.
>
> ❌ **No puntúa** una justificación genérica que podría pegarse en cualquier proyecto
> (ej.: *"usé boundedElastic porque es una buena práctica para operaciones bloqueantes"*).
> ✅ **Sí puntúa** una justificación anclada a tu código (ej.: *"en `ProductoService`
> línea 34 envolví `productoRepository.findAll()` porque Hibernate abre la conexión
> JDBC en el hilo llamante; al probarlo sin `subscribeOn` vi en el log el hilo
> `reactor-http-nio-2`, que es el event loop de Netty"*).
>
> Estas mismas preguntas se te harán en la **defensa oral**.

---

## Datos

- **Nombre:** Fabricio Fernando Baquero López
- **Cédula:** 1719271643
- **NN (dos últimos dígitos):** 43
- **Categoría asignada (según el último dígito):** Café

---

## Fase 1 — Configuración y perfiles

**1.1** ¿Qué archivo activa el perfil `prod` y qué línea exacta lo hace?
> El perfil `prod` se activa desde el archivo `src/main/resources/application.properties` mediante la propiedad:
>
> ```properties
> spring.profiles.active=prod
> ```
>

**1.2** Pega la línea del log de arranque donde se ve tu puerto y el perfil activo.

```text
The following 1 profile is active: "prod"
Netty started on port 8143 (http)
```

**1.3** ¿Qué habría pasado si dejabas `ddl-auto=create-drop` en lugar de `update`?
Responde pensando en tus datos sembrados.

> Si hubiera utilizado spring.jpa.hibernate.ddl-auto=create-drop, la estructura de la base de datos se eliminaría al finalizar la aplicación y se volvería a crear en cada ejecución. Esto ocasionaría la pérdida de los datos sembrados y tendría que recrearlos en cada inicio. Por ello configuré update, para conservar el esquema y los datos existentes entre ejecuciones.

**1.4** ¿Levantaste PostgreSQL con `compose.yaml` (Opción A) o con una instalación local
(Opción B)? ¿Qué ventaja tiene la que elegiste?

> Levanté PostgreSQL utilizando Docker mediante `compose.yaml`. Elegí esta opción porque permite disponer de un entorno reproducible y aislado, facilita la configuración de la base de datos y evita instalar PostgreSQL directamente en el sistema operativo.
---

## Fase 2 — Persistencia con JPA/Hibernate

**2.1** ¿Cuál es el nombre exacto de tu tabla y de dónde salió ese nombre?

> La tabla se llama exactamente tbl_productos_base_43. El nombre proviene de la nomenclatura solicitada en el examen, utilizando los dos últimos dígitos de mi cédula (43), por lo que la entidad ProductoEntity quedó mapeada con @Table(name = "tbl_productos_base_43").

**2.2** Pega la salida de `psql -d agrosmart_db -c "\d tbl_productos_base_NN"` y
señala dónde se ve la restricción `unique` y el `length` de 120.

```Table "public.tbl_productos_base_43"

Column               Type
----------------------------------------------
id_producto          bigint
categoria            character varying(40)
correos_notificacion character varying(500)
nombre_producto      character varying(120)
precio_usd           numeric(10,2)
stock_kg             integer

Indexes:
"tbl_productos_base_43_pkey" PRIMARY KEY
"..."
UNIQUE CONSTRAINT (nombre_producto)

La restricción UNIQUE se observa en el índice UNIQUE CONSTRAINT sobre nombre_producto. El tamaño máximo de 120 caracteres se observa en la definición character varying(120) de la columna nombre_producto.

```

**2.3** ¿Por qué usaste `BigDecimal` y no `double` para `precio_usd`? Relaciónalo con el
tipo que generó Hibernate en PostgreSQL.

> Utilicé BigDecimal porque representa valores monetarios sin errores de precisión. Hibernate generó la columna PostgreSQL como numeric(10,2), que corresponde naturalmente a BigDecimal y evita los errores de redondeo que tendría un tipo double.

**2.4** ¿Cómo hiciste idempotente tu siembra y qué pasaría en el segundo arranque si no
lo fuera? (piensa en la restricción `unique` de `nombre_producto`)

> La siembra es idempotente porque antes de insertar registros se verifica si la tabla ya contiene datos mediante un count(). En el segundo arranque aparece el mensaje "La tabla ya contiene datos. No se realizó una nueva siembra.", evitando insertar nuevamente productos con el mismo nombre. Si no fuera idempotente, PostgreSQL produciría un error por violar la restricción UNIQUE de nombre_producto.

---

## Fase 3 — Modelo inmutable y lógica funcional

**3.1** ¿Por qué tienes **dos** clases (`ProductoEntity` y `Producto`) en lugar de una?
¿Qué te impide hacer inmutable directamente la entidad de Hibernate?

> Separé ProductoEntity y Producto porque cumplen responsabilidades diferentes. ProductoEntity representa la tabla tbl_productos_base_43 y necesita constructor vacío, setters y atributos modificables para que Hibernate pueda crear y materializar las entidades recuperadas desde PostgreSQL. En cambio, Producto es mi modelo de dominio y lo declaré como final, con atributos private final, sin setters y con copias defensivas. Hacer directamente inmutable la entidad dificultaría el trabajo de Hibernate, que necesita construir el objeto y asignar los valores obtenidos de la base de datos.

**3.2** Escribe el código exacto de **tus dos** copias defensivas e indica en qué línea
está cada una.

```java
// Copia defensiva de entrada, ubicada en el constructor de Producto,
// aproximadamente en la línea 27 de Producto.java.
this.correosNotificacion = new ArrayList<>(correosNotificacion);

// Copia defensiva de salida, ubicada en el getter,
// aproximadamente entre las líneas 47 y 49 de Producto.java.
public List<String> getCorreosNotificacion() {
    return Collections.unmodifiableList(
            new ArrayList<>(correosNotificacion)
    );
}
```

**3.3** ¿Por qué la copia defensiva **solo en el getter** no sería suficiente? Describe
el ataque concreto que quedaría abierto sobre **tu** clase.

> Una copia defensiva únicamente en el getter no protegería el objeto durante su construcción. Si guardara directamente la lista recibida en el constructor, otra clase podría conservar esa misma referencia y modificarla después. Por ejemplo, podría crear un Producto con una lista que contiene un correo y luego ejecutar correos.add("intruso@mail.com"). Sin la copia defensiva de entrada, ese nuevo correo aparecería también dentro del estado interno del Producto, aunque el objeto no tenga setters. Por eso copié la lista tanto al recibirla como al devolverla.

**3.4** ¿Cómo implementaste `A_MAYUSCULAS` para no mutar el `Producto` recibido?

```java
public static final Function<Producto, Producto> A_MAYUSCULAS =
        producto -> new Producto(
                producto.getId(),
                producto.getNombre().toUpperCase(),
                producto.getCategoria(),
                producto.getPrecioUsd(),
                producto.getCorreosNotificacion()
        );

La función no modifica el objeto recibido. Construye una nueva instancia de Producto, conserva sus demás atributos y transforma únicamente el nombre mediante toUpperCase().
```

---

## Fase 4 — Servicio reactivo y aislamiento del bloqueo

**4.1** Pega tu método `obtenerProductosComercializables()` completo.

```java
public Flux<Producto> obtenerProductosComercializables() {

    // Difiere la consulta bloqueante hasta que exista una suscripción.
    return Mono.fromCallable(repository::findAll)

            // Ejecuta la consulta JPA en boundedElastic para no bloquear
            // el event loop de Netty.
            .subscribeOn(Schedulers.boundedElastic())

            // Convierte la lista obtenida por JPA en un flujo de entidades.
            .flatMapMany(Flux::fromIterable)

            // Transforma cada entidad JPA al modelo de dominio inmutable.
            .map(ProductoMapper::toDominio)

            // Crea una nueva instancia con el nombre en mayúsculas.
            .map(ProductoFilters.A_MAYUSCULAS)

            // Conserva únicamente productos con precio mayor que cero
            // y al menos un correo de notificación.
            .filter(ProductoFilters.IS_VALID)

            // Registra el producto procesado sin modificarlo.
            .doOnNext(ProductoFilters.LOG_PRODUCTO)

            // Emite un producto genérico cuando el filtro deja el flujo vacío.
            .defaultIfEmpty(PRODUCTO_GENERICO);
}
```

**4.2** ¿Qué pasa **exactamente** si eliminas
`.subscribeOn(Schedulers.boundedElastic())` de ese método? Si lo probaste, indica qué
hilo aparecía en el log antes y después.

> No eliminé subscribeOn(Schedulers.boundedElastic()) durante la implementación final, por lo que no tengo una comparación real de nombres de hilos antes y después. En mi método, repository.findAll() utiliza JPA/Hibernate y realiza una consulta JDBC bloqueante. Sin boundedElastic, la operación se ejecutaría en el hilo que realiza la suscripción. Cuando el flujo se invoca desde WebFlux, podría terminar bloqueando un hilo del event loop de Netty, retrasando también otras peticiones atendidas por ese mismo hilo. Con boundedElastic, la consulta se desplaza a un hilo preparado para operaciones bloqueantes.

**4.3** ¿Por qué `Mono.fromCallable(...)` y no `Mono.just(repository.findAll())`?
(pista: cuándo se ejecuta cada uno)

> En ProductoService utilicé Mono.fromCallable(repository::findAll) porque difiere la ejecución de repository.findAll() hasta que alguien se suscribe al flujo. En cambio, con Mono.just(repository.findAll()), la consulta se ejecutaría inmediatamente al construir el Mono, antes de la suscripción y antes de que subscribeOn pueda trasladarla a boundedElastic. Por eso fromCallable permite integrar correctamente la operación bloqueante dentro del flujo reactivo.

**4.4** En **tu** código, ¿dónde usaste `defaultIfEmpty` y dónde `switchIfEmpty`, y por
qué no son intercambiables en esos dos lugares?

> Utilicé defaultIfEmpty(PRODUCTO_GENERICO) al final de obtenerProductosComercializables(). Si los productos recuperados son descartados por IS_VALID, el Flux queda vacío y emito un producto genérico como valor alternativo.

Utilicé switchIfEmpty(Mono.error(new ProductoNoEncontradoException(id))) en buscarPorId(Long id). Cuando repository.findById(id) devuelve un Optional vacío, el flujo cambia a un Mono de error.

No son intercambiables porque en el primer caso necesito emitir un valor normal de respaldo, mientras que en el segundo necesito cambiar el flujo a una señal de error que posteriormente WebFlux traduce a HTTP 404.

**4.5** ¿Por qué `doOnNext` no sirve para transformar el elemento, si aparentemente
"recibe" el producto?

> doOnNext recibe el producto únicamente para ejecutar un efecto secundario, en mi caso ProductoFilters.LOG_PRODUCTO, que imprime el id y el nombre. El operador no utiliza el valor retornado por el Consumer y mantiene el mismo elemento dentro del flujo. Para transformar el producto utilicé map(ProductoFilters.A_MAYUSCULAS), porque map sí reemplaza cada elemento por el nuevo valor producido por la función.

---

## Fase 5 — Módulo de IA con LangChain4j

**5.1** Pega tu interfaz `AgroSmartAIService` completa.

```java
package ec.edu.espe.agrosmart.ai;

import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.spring.AiService;

@AiService
public interface AgroSmartAIService {

    @UserMessage("""
            Redacta una frase publicitaria de máximo 100 caracteres para vender \
            {{producto}} dirigido a {{audiencia}}.""")
    String generarPublicidad(
            @V("producto") String producto,
            @V("audiencia") String audiencia
    );
}
```

**5.2** ¿Qué hace `@V("producto")` y qué pasaría si lo quitaras dejando solo el
parámetro?

> @V("producto") vincula el parámetro Java producto con la variable {{producto}} utilizada dentro de @UserMessage. De la misma manera, @V("audiencia") vincula el segundo parámetro con {{audiencia}}. Si quitara la anotación y dejara únicamente el parámetro, LangChain4j podría no encontrar de forma confiable el valor correspondiente a la variable del prompt, especialmente si los nombres de parámetros no están disponibles en tiempo de ejecución, y el mensaje no se construiría correctamente.

**5.3** ¿En qué archivo y con qué líneas configuraste el modelo? ¿Por qué **no** hizo
falta declarar un `@Bean`?

> Configuré el modelo al final de src/main/resources/application-prod.properties, aproximadamente entre las líneas 14 y 19:
```java
langchain4j.open-ai.chat-model.api-key=demo
langchain4j.open-ai.chat-model.model-name=gpt-4o-mini
langchain4j.open-ai.chat-model.timeout=30s
langchain4j.open-ai.chat-model.log-requests=true
langchain4j.open-ai.chat-model.log-responses=true
logging.level.dev.langchain4j=DEBUG
```
> No declaré ningún @Bean porque añadí los starters langchain4j-open-ai-spring-boot4-starter y langchain4j-spring-boot4-starter. Estos starters leen las propiedades, crean el modelo automáticamente y detectan mi interfaz anotada con @AiService. En el log de arranque se confirmó con el mensaje Identified candidate component class: ... AgroSmartAIService.class.
> 
**5.4** ¿Por qué la llamada a la IA también necesita `boundedElastic`, si no es una
consulta a base de datos?

> Aunque no sea una consulta JDBC, aiService.generarPublicidad(producto, audiencia) realiza una llamada HTTP síncrona al proveedor externo y espera la respuesta. Durante ese tiempo el hilo queda bloqueado por red, timeout o procesamiento del proveedor. En mi método generarPublicidad() envolví esa llamada con Mono.fromCallable(...) y la ejecuté en Schedulers.boundedElastic() para evitar bloquear el event loop de Netty. También agregué un timeout de 30 segundos.

**5.5** Si tu proveedor devolvió un error durante el examen, pega el mensaje real y la
respuesta que produjo tu `onErrorResume`.

```
Error real del proveedor:
AuthenticationException

Respuesta generada por onErrorResume:
Publicidad no disponible en este momento (AuthenticationException)

El error no se propagó hasta derribar el endpoint porque onErrorResume lo transformó en un Mono con un mensaje controlado.
```

---

## Fase 6 — API reactiva con WebFlux

**6.1** Pega la salida real de tus cuatro `curl`.

```
PS> curl.exe http://localhost:8143/api/productos

[{"id":1,"nombre":"CAFÉ ARÁBIGO DE ALTURA","categoria":"Café","precioUsd":8.50,"correosNotificacion":["ventas@agrosmart.ec","compras@agrosmart.ec"]},{"id":2,"nombre":"CAFÉ ORGÁNICO LAVADO","categoria":"Café","precioUsd":10.75,"correosNotificacion":["organico@agrosmart.ec"]},{"id":3,"nombre":"CAFÉ ROBUSTA TOSTADO","categoria":"Café","precioUsd":7.25,"correosNotificacion":["ventas@agrosmart.ec"]}]


PS> curl.exe http://localhost:8143/api/productos/1

{"id":1,"nombre":"Café arábigo de altura","categoria":"Café","precioUsd":8.50,"correosNotificacion":["ventas@agrosmart.ec","compras@agrosmart.ec"]}


PS> curl.exe -i http://localhost:8143/api/productos/9999

HTTP/1.1 404 Not Found
Content-Type: application/json
Content-Length: 127

{"timestamp":"2026-08-01T01:30:37.765Z","path":"/api/productos/9999","status":404,"error":"Not Found","requestId":"2fe7f3fb-3"}


PS> curl.exe "http://localhost:8143/api/agrosmart/publicidad?producto=Cacao%20fino%20de%20aroma&audiencia=exportadores%20europeos"

Publicidad no disponible en este momento (AuthenticationException)
```

**6.2** ¿Cómo lograste que el id inexistente responda **404** y no 500?

> Anoté mi clase ProductoNoEncontradoException con @ResponseStatus(HttpStatus.NOT_FOUND). En ProductoService.buscarPorId() utilizo switchIfEmpty(Mono.error(new ProductoNoEncontradoException(id))). Cuando el repositorio devuelve un Optional vacío, el flujo emite esa excepción y Spring WebFlux lee la anotación para responder con HTTP 404 en lugar de tratarla como un error interno 500.

**6.3** ¿Qué pasaría si tu controlador devolviera `List<Producto>` en lugar de
`Flux<Producto>`? ¿Seguiría compilando? ¿Seguiría siendo no bloqueante?

> Si cambiara únicamente el tipo de retorno a List<Producto> y continuara retornando el Flux producido por el servicio, el código no compilaría porque Flux<Producto> no es compatible con List<Producto>. Para obtener una lista tendría que materializar el flujo, probablemente mediante collectList() y luego block(). Eso podría compilar, pero rompería el modelo no bloqueante porque el hilo esperaría de forma síncrona hasta completar el flujo. Por eso mi controlador devuelve directamente Flux<Producto> y Mono<Producto>.

---

## Fase 7 — Pruebas unitarias

**7.1** Pega la salida real de tus pruebas (`./mvnw test` o `./gradlew test`).

```
[INFO] Running ec.edu.espe.agrosmart.AgrosmartApplicationTests
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0

[INFO] Running ec.edu.espe.agrosmart.domain.function.ProductoFiltersTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0

[INFO] Running ec.edu.espe.agrosmart.domain.model.ProductoTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0

[INFO] Running ec.edu.espe.agrosmart.service.ProductoServiceTest
Producto procesado: id=1, nombre=CAFÉ ARÁBIGO DE ALTURA
Producto procesado: id=2, nombre=CAFÉ ORGÁNICO LAVADO
Producto procesado: id=3, nombre=CAFÉ ROBUSTA TOSTADO
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0

[INFO] Running ec.edu.espe.agrosmart.service.PublicidadServiceTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0

[INFO] Results:

[INFO] Tests run: 12, Failures: 0, Errors: 0, Skipped: 0

[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 10.471 s
```

**7.2** ¿Cuántos productos espera tu `expectNextCount(...)` y por qué ese número
concreto? Relaciónalo con tu semilla.

> Mi prueba espera exactamente expectNextCount(3). Preparé en el repositorio simulado cinco entidades equivalentes a mi escenario de siembra: tres productos válidos de Café y dos inválidos. Uno de los inválidos tiene precio cero y el otro correos vacíos. ProductoFilters.IS_VALID descarta esos dos registros, por lo que el flujo comercializable emite solamente tres productos.
 
**7.3** ¿Por qué mockeaste `ProductoRepository` en lugar de dejar que la prueba consulte
PostgreSQL?

> Mockeé ProductoRepository con Mockito para que ProductoServiceTest fuera una prueba unitaria y no dependiera de PostgreSQL, Docker, el puerto 55432 ni datos externos. Mediante Mockito.when(repository.findAll()) y Mockito.when(repository.findById(...)) controlo exactamente los datos entregados al servicio. Así pruebo únicamente mi pipeline reactivo y puedo repetir la prueba en cualquier entorno.

**7.4** ¿Qué demuestra `assertNotSame` que `assertEquals` **no** demuestra en tu prueba
de copia defensiva?

> assertEquals demuestra que dos listas contienen los mismos elementos, pero no indica si son el mismo objeto. assertNotSame verifica que la lista original y la lista devuelta por getCorreosNotificacion() tienen referencias diferentes. Esto demuestra que el getter crea una copia defensiva y no expone directamente la colección interna de Producto.

**7.5** ¿Por qué una prueba de un `Flux` que no llama a `verifyComplete()` (o a
`verify()`) no está probando nada?

> Los flujos de Reactor son perezosos y no ejecutan su pipeline hasta que existe una suscripción. StepVerifier.create(flujo) únicamente prepara el escenario. La llamada a verifyComplete() o verify() realiza la suscripción, espera las señales y comprueba las expectativas. Sin esa llamada, el repositorio simulado, los operadores y las aserciones del flujo no se ejecutarían realmente.

---

## Fase 8 — Integración y cierre

**8.1** Pega tu `git log --oneline --graph --all`.

```
* 9b18056 (HEAD -> feature/pruebas, origin/feature/pruebas) test: agrega pruebas del modelo, logica funcional, flujo reactivo e ia
*   b754758 (origin/main, origin/HEAD, main) Merge pull request #6 from Bakerlop/feature/api-reactiva
|\
| * b1093d7 (origin/feature/api-reactiva, feature/api-reactiva) feat: expone endpoints reactivos y de publicidad
|/
*   9aa7703 Merge pull request #5 from Bakerlop/feature/ia-langchain4j
|\
| * d35659b (origin/feature/ia-langchain4j, feature/ia-langchain4j) feat: integra langchain4j para publicidad de productos
|/
*   c7d6ded Merge pull request #4 from Bakerlop/feature/servicio-reactivo
|\
| * e8768c7 (origin/feature/servicio-reactivo, feature/servicio-reactivo) feat: implementa servicio reactivo con boundedElastic y operadores
|/
*   baf8ecd Merge pull request #3 from Bakerlop/feature/modelo-inmutable
|\
| * 8cce057 (origin/feature/modelo-inmutable, feature/modelo-inmutable) feat: agrega modelo inmutable de producto y logica funcional
|/
*   cd40694 Merge pull request #2 from Bakerlop/feature/persistencia-jpa
|\
| * ebdd3f8 (origin/feature/persistencia-jpa, feature/persistencia-jpa) feat: agrega entidad jpa de productos y siembra de datos
|/
*   7bbbb60 Merge pull request #1 from Bakerlop/feature/config-perfiles
|\
| * f5072a8 (origin/feature/config-perfiles, feature/config-perfiles) docs: completa decisiones y evidencia de la fase 1
| * 69a73df docs: completa decisiones de la fase 1
| * dca8a5b chore: actualiza configuracion de perfiles
| * 849472d fix: configura conexion PostgreSQL en puerto 55432
| * 9470753 build: configura proyecto Spring Boot y PostgreSQL
|/
```

**8.2** ¿Qué fase te tomó más tiempo del previsto y por qué?

> La fase que me tomó más tiempo fue la Fase 7 de pruebas unitarias. Tuve que configurar correctamente JUnit 5, Mockito y Reactor Test, ubicar las clases dentro de src/test/java, crear entidades simuladas y comprobar los flujos con StepVerifier. También corregí inicialmente la ubicación de ProductoTest, porque lo había creado dentro de src/main/java, y después verifiqué las doce pruebas hasta obtener Failures: 0, Errors: 0 y BUILD SUCCESS.

**8.3** Si tuvieras 30 minutos más, ¿qué mejorarías **primero** de tu entrega y por qué
esa y no otra?

> Primero eliminaría la dependencia de PostgreSQL de AgrosmartApplicationTests, porque la ejecución completa actualmente levanta el contexto con el perfil prod y se conecta a la base de datos. Mis pruebas específicas de ProductoService sí usan Mockito y no dependen de PostgreSQL, pero mejoraría la prueba de contexto utilizando un perfil exclusivo de pruebas o reemplazando los beans externos. Elegiría esta mejora antes que cambios visuales porque aumentaría la portabilidad y permitiría ejecutar toda la suite sin Docker ni servicios externos.

**8.4** Declara honestamente qué herramientas consultaste durante el examen
(documentación, apuntes, asistentes de IA) y para qué. **Esta declaración no descuenta
puntaje**; su omisión o falsedad sí constituye falta de honestidad académica.

> Durante el desarrollo consulté las instrucciones y ejemplos proporcionados en el README.md, la documentación oficial de Spring Boot, Project Reactor, LangChain4j, JUnit 5 y Mockito. También utilicé ChatGPT como asistente de IA para aclarar mensajes de error, revisar configuraciones, orientar la estructura de clases y pruebas, y verificar comandos de Git, Docker y Maven. Probé cada cambio en mi propio proyecto, revisé las salidas reales de terminal y confirmé el funcionamiento mediante compilación, curl, PostgreSQL y las doce pruebas ejecutadas.
