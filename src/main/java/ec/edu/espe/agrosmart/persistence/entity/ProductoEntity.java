package ec.edu.espe.agrosmart.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "tbl_productos_base_43")
public class ProductoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto")
    private Long idProducto;

    @Column(name = "nombre_producto", length = 120, nullable = false, unique = true)
    private String nombreProducto;

    @Column(name = "precio_usd", precision = 10, scale = 2)
    private BigDecimal precioUsd;

    @Column(name = "stock_kg", nullable = false)
    private Integer stockKg;

    @Column(name = "categoria", length = 40)
    private String categoria;

    @Column(name = "correos_notificacion", length = 500)
    private String correosNotificacion;

    public ProductoEntity() {
    }

    public Long getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(Long idProducto) {
        this.idProducto = idProducto;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto = nombreProducto;
    }

    public BigDecimal getPrecioUsd() {
        return precioUsd;
    }

    public void setPrecioUsd(BigDecimal precioUsd) {
        this.precioUsd = precioUsd;
    }

    public Integer getStockKg() {
        return stockKg;
    }

    public void setStockKg(Integer stockKg) {
        this.stockKg = stockKg;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getCorreosNotificacion() {
        return correosNotificacion;
    }

    public void setCorreosNotificacion(String correosNotificacion) {
        this.correosNotificacion = correosNotificacion;
    }
}