package ec.edu.espe.agrosmart.domain.function;

import ec.edu.espe.agrosmart.domain.model.Producto;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public final class ProductoFilters {

    private ProductoFilters() {
    }

    public static final Predicate<Producto> IS_VALID =
            producto -> producto.getPrecioUsd().signum() > 0
                    && !producto.getCorreosNotificacion().isEmpty();

    public static final Consumer<Producto> LOG_PRODUCTO =
            producto -> System.out.println(
                    "Producto procesado: id="
                            + producto.getId()
                            + ", nombre="
                            + producto.getNombre()
            );

    public static final Function<Producto, Producto> A_MAYUSCULAS =
            producto -> new Producto(
                    producto.getId(),
                    producto.getNombre().toUpperCase(),
                    producto.getCategoria(),
                    producto.getPrecioUsd(),
                    producto.getCorreosNotificacion()
            );
}