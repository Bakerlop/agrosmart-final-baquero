package ec.edu.espe.agrosmart.domain.function;

import ec.edu.espe.agrosmart.domain.model.Producto;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductoFiltersTest {

    @Test
    void isValid_conPrecioPositivoYCorreos_debeRetornarTrue() {
        // Arrange
        Producto producto = new Producto(
                1L,
                "Café arábigo",
                "Café",
                new BigDecimal("8.50"),
                List.of("ventas@agrosmart.ec")
        );

        // Act
        boolean resultado = ProductoFilters.IS_VALID.test(producto);

        // Assert
        assertTrue(resultado);
    }

    @Test
    void isValid_conPrecioCero_debeRetornarFalse() {
        // Arrange
        Producto producto = new Producto(
                2L,
                "Café inválido",
                "Café",
                BigDecimal.ZERO,
                List.of("ventas@agrosmart.ec")
        );

        // Act
        boolean resultado = ProductoFilters.IS_VALID.test(producto);

        // Assert
        assertFalse(resultado);
    }

    @Test
    void isValid_conListaDeCorreosVacia_debeRetornarFalse() {
        // Arrange
        Producto producto = new Producto(
                3L,
                "Café sin correo",
                "Café",
                new BigDecimal("9.00"),
                List.of()
        );

        // Act
        boolean resultado = ProductoFilters.IS_VALID.test(producto);

        // Assert
        assertFalse(resultado);
    }
}