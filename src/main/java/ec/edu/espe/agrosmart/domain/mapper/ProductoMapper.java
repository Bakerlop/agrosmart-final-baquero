package ec.edu.espe.agrosmart.domain.mapper;

import ec.edu.espe.agrosmart.domain.model.Producto;
import ec.edu.espe.agrosmart.persistence.entity.ProductoEntity;

import java.util.Arrays;
import java.util.List;

public final class ProductoMapper {

    private ProductoMapper() {
    }

    public static Producto toDominio(ProductoEntity entity) {

        List<String> correos = entity.getCorreosNotificacion() == null
                || entity.getCorreosNotificacion().isBlank()
                ? List.of()
                : Arrays.stream(entity.getCorreosNotificacion().split(","))
                .map(String::trim)
                .filter(correo -> !correo.isEmpty())
                .toList();

        return new Producto(
                entity.getIdProducto(),
                entity.getNombreProducto(),
                entity.getCategoria(),
                entity.getPrecioUsd(),
                correos
        );
    }
}