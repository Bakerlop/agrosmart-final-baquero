package ec.edu.espe.agrosmart.service;

import ec.edu.espe.agrosmart.ai.AgroSmartAIService;
import ec.edu.espe.agrosmart.domain.exception.ProductoNoEncontradoException;
import ec.edu.espe.agrosmart.domain.function.ProductoFilters;
import ec.edu.espe.agrosmart.domain.mapper.ProductoMapper;
import ec.edu.espe.agrosmart.domain.model.Producto;
import ec.edu.espe.agrosmart.persistence.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

@Service
public class ProductoService {

    private static final Producto PRODUCTO_GENERICO = new Producto(
            0L,
            "PRODUCTO GENÉRICO",
            "Café",
            new BigDecimal("1.00"),
            List.of("notificaciones@agrosmart.ec")
    );

    private final ProductoRepository repository;
    private final AgroSmartAIService aiService;

    public ProductoService(
            ProductoRepository repository,
            AgroSmartAIService aiService
    ) {
        this.repository = repository;
        this.aiService = aiService;
    }

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

    public Mono<Producto> buscarPorId(Long id) {

        // Difiere la consulta findById hasta que alguien se suscriba.
        return Mono.fromCallable(() -> repository.findById(id))

                // Aísla la operación bloqueante de JPA en boundedElastic.
                .subscribeOn(Schedulers.boundedElastic())

                // Convierte Optional vacío en Mono vacío y Optional con valor
                // en un Mono que contiene la entidad.
                .flatMap(Mono::justOrEmpty)

                // Convierte la entidad encontrada al modelo de dominio.
                .map(ProductoMapper::toDominio)

                // Si el Mono queda vacío, cambia a un flujo de error.
                .switchIfEmpty(
                        Mono.error(new ProductoNoEncontradoException(id))
                );
    }

    public Mono<String> generarPublicidad(
            String producto,
            String audiencia
    ) {

        // Difiere la llamada bloqueante al proveedor de IA
        // hasta que exista una suscripción.
        return Mono.fromCallable(
                        () -> aiService.generarPublicidad(
                                producto,
                                audiencia
                        )
                )

                // La llamada HTTP de LangChain4j puede bloquear el hilo,
                // por eso se ejecuta fuera del event loop.
                .subscribeOn(Schedulers.boundedElastic())

                // Cancela la operación si el proveedor tarda más de 30 segundos.
                .timeout(Duration.ofSeconds(30))

                // Convierte cualquier fallo externo en una respuesta controlada.
                .onErrorResume(error -> Mono.just(
                        "Publicidad no disponible en este momento ("
                                + error.getClass().getSimpleName()
                                + ")"
                ));
    }
}