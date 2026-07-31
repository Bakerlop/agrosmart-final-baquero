package ec.edu.espe.agrosmart.persistence.repository;

import ec.edu.espe.agrosmart.persistence.entity.ProductoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoRepository extends JpaRepository<ProductoEntity, Long> {
}