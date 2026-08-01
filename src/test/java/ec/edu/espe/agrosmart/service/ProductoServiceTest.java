package ec.edu.espe.agrosmart.service;

import ec.edu.espe.agrosmart.domain.exception.ProductoNoEncontradoException;
import ec.edu.espe.agrosmart.domain.model.Producto;
import ec.edu.espe.agrosmart.persistence.entity.ProductoEntity;
import ec.edu.espe.agrosmart.persistence.repository.ProductoRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

class ProductoServiceTest {

    @Test
    void obtenerProductosComercializables_conTresValidosYDosInvalidos_debeEmitirSoloLosValidos() {
        // Arrange
        ProductoRepository repository = Mockito.mock(ProductoRepository.class);

        Mockito.when(repository.findAll()).thenReturn(List.of(
                crearProducto(
                        1L,
                        "Café arábigo de altura",
                        new BigDecimal("8.50"),
                        "ventas@agrosmart.ec"
                ),
                crearProducto(
                        2L,
                        "Café orgánico lavado",
                        new BigDecimal("10.75"),
                        "organico@agrosmart.ec"
                ),
                crearProducto(
                        3L,
                        "Café robusta tostado",
                        new BigDecimal("7.25"),
                        "ventas@agrosmart.ec"
                ),
                crearProducto(
                        4L,
                        "Café precio cero",
                        BigDecimal.ZERO,
                        "ventas@agrosmart.ec"
                ),
                crearProducto(
                        5L,
                        "Café sin correos",
                        new BigDecimal("9.00"),
                        ""
                )
        ));

        ProductoService service = new ProductoService(repository, null);

        // Act
        Flux<Producto> flujo = service.obtenerProductosComercializables();

        // Assert
        StepVerifier.create(flujo)
                .expectNextCount(3)
                .verifyComplete();
    }

    @Test
    void obtenerProductosComercializables_conTodosInvalidos_debeEmitirProductoGenerico() {
        // Arrange
        ProductoRepository repository = Mockito.mock(ProductoRepository.class);

        Mockito.when(repository.findAll()).thenReturn(List.of(
                crearProducto(
                        1L,
                        "Café precio cero",
                        BigDecimal.ZERO,
                        "ventas@agrosmart.ec"
                ),
                crearProducto(
                        2L,
                        "Café sin correos",
                        new BigDecimal("9.00"),
                        ""
                )
        ));

        ProductoService service = new ProductoService(repository, null);

        // Act
        Flux<Producto> flujo = service.obtenerProductosComercializables();

        // Assert
        StepVerifier.create(flujo)
                .expectNextMatches(producto ->
                        producto.getId().equals(0L)
                                && producto.getNombre().equals("PRODUCTO GENÉRICO")
                )
                .verifyComplete();
    }

    @Test
    void buscarPorId_conIdInexistente_debeEmitirProductoNoEncontradoException() {
        // Arrange
        ProductoRepository repository = Mockito.mock(ProductoRepository.class);

        Mockito.when(repository.findById(9999L))
                .thenReturn(Optional.empty());

        ProductoService service = new ProductoService(repository, null);

        // Act
        var resultado = service.buscarPorId(9999L);

        // Assert
        StepVerifier.create(resultado)
                .expectError(ProductoNoEncontradoException.class)
                .verify();
    }

    private ProductoEntity crearProducto(
            Long id,
            String nombre,
            BigDecimal precio,
            String correos
    ) {
        ProductoEntity entity = new ProductoEntity();
        entity.setIdProducto(id);
        entity.setNombreProducto(nombre);
        entity.setPrecioUsd(precio);
        entity.setStockKg(100);
        entity.setCategoria("Café");
        entity.setCorreosNotificacion(correos);
        return entity;
    }
}