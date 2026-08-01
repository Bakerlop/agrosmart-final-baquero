package ec.edu.espe.agrosmart.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProductoTest {

    @Test
    void getters_conDatosDelConstructor_debenDevolverLosMismosValores() {

        // Arrange
        List<String> correos = List.of("ventas@agrosmart.ec");

        Producto producto = new Producto(
                1L,
                "Café arábigo",
                "Café",
                new BigDecimal("8.50"),
                correos
        );

        // Act & Assert
        assertEquals(1L, producto.getId());
        assertEquals("Café arábigo", producto.getNombre());
        assertEquals("Café", producto.getCategoria());
        assertEquals(new BigDecimal("8.50"), producto.getPrecioUsd());
        assertEquals(correos, producto.getCorreosNotificacion());
    }

    @Test
    void getCorreosNotificacion_alMutarLaListaOriginal_noDebeAfectarAlProducto() {

        // Arrange
        List<String> correos = new ArrayList<>();
        correos.add("ventas@agrosmart.ec");

        Producto producto = new Producto(
                1L,
                "Café arábigo",
                "Café",
                new BigDecimal("8.50"),
                correos
        );

        // Act
        correos.add("intruso@mail.com");

        // Assert
        assertEquals(1, producto.getCorreosNotificacion().size());
        assertNotSame(correos, producto.getCorreosNotificacion());
    }

    @Test
    void getCorreosNotificacion_alIntentarModificarLaListaDevuelta_debeLanzarExcepcion() {

        // Arrange
        List<String> correos = new ArrayList<>();
        correos.add("ventas@agrosmart.ec");

        Producto producto = new Producto(
                1L,
                "Café arábigo",
                "Café",
                new BigDecimal("8.50"),
                correos
        );

        // Act
        List<String> lista = producto.getCorreosNotificacion();

        // Assert
        assertThrows(
                UnsupportedOperationException.class,
                () -> lista.add("nuevo@mail.com")
        );
    }
}