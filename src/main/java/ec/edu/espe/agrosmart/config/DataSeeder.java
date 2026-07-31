package ec.edu.espe.agrosmart.config;

import ec.edu.espe.agrosmart.persistence.entity.ProductoEntity;
import ec.edu.espe.agrosmart.persistence.repository.ProductoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    private final ProductoRepository productoRepository;

    public DataSeeder(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    @Override
    public void run(String... args) {

        if (productoRepository.count() == 0) {

            ProductoEntity producto1 = crearProducto(
                    "Café arábigo de altura",
                    new BigDecimal("8.50"),
                    120,
                    "Café",
                    "ventas@agrosmart.ec,compras@agrosmart.ec"
            );

            ProductoEntity producto2 = crearProducto(
                    "Café orgánico lavado",
                    new BigDecimal("10.75"),
                    85,
                    "Café",
                    "organico@agrosmart.ec"
            );

            ProductoEntity producto3 = crearProducto(
                    "Café robusta tostado",
                    new BigDecimal("7.25"),
                    150,
                    "Café",
                    "ventas@agrosmart.ec"
            );

            ProductoEntity producto4 = crearProducto(
                    "Café especial precio cero",
                    BigDecimal.ZERO,
                    40,
                    "Café",
                    "alertas@agrosmart.ec"
            );

            ProductoEntity producto5 = crearProducto(
                    "Café sin correos registrados",
                    new BigDecimal("9.30"),
                    60,
                    "Café",
                    ""
            );

            productoRepository.saveAll(List.of(
                    producto1,
                    producto2,
                    producto3,
                    producto4,
                    producto5
            ));

            System.out.println("Se sembraron 5 productos de la categoría Café.");
        } else {
            System.out.println(
                    "La tabla ya contiene datos. No se realizó una nueva siembra."
            );
        }
    }

    private ProductoEntity crearProducto(
            String nombreProducto,
            BigDecimal precioUsd,
            Integer stockKg,
            String categoria,
            String correosNotificacion
    ) {
        ProductoEntity producto = new ProductoEntity();
        producto.setNombreProducto(nombreProducto);
        producto.setPrecioUsd(precioUsd);
        producto.setStockKg(stockKg);
        producto.setCategoria(categoria);
        producto.setCorreosNotificacion(correosNotificacion);
        return producto;
    }
}