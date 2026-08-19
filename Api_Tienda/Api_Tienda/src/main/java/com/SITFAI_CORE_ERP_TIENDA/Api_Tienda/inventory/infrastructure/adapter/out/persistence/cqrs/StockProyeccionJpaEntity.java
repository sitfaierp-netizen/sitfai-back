package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.out.persistence.cqrs;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import java.sql.Types;

/**
 * JPA Entity para el Modelo de Lectura (Read Model) de CQRS.
 * Mapea directamente a la vista materializada o tabla desnormalizada 'inventory_stock_view'.
 */
@Entity
@Table(name = "inventory_stock_view")
@IdClass(StockProyeccionId.class)
public class StockProyeccionJpaEntity {

    @Id
    @JdbcTypeCode(Types.BINARY)
    @Column(name = "empresa_id", columnDefinition = "BINARY(16)", nullable = false)
    private UUID empresaId;

    @Id
    @JdbcTypeCode(Types.BINARY)
    @Column(name = "bodega_id", columnDefinition = "BINARY(16)", nullable = false)
    private UUID bodegaId;

    @Id
    @JdbcTypeCode(Types.BINARY)
    @Column(name = "producto_id", columnDefinition = "BINARY(16)", nullable = false)
    private UUID productoId;

    @Column(name = "cantidad_total", precision = 19, scale = 4, nullable = false)
    private BigDecimal cantidadTotal;

    @Column(name = "ultima_actualizacion", nullable = false)
    private Instant ultimaActualizacion;

    // Constructores, Getters y Setters
    protected StockProyeccionJpaEntity() {}

    public StockProyeccionJpaEntity(UUID empresaId, UUID bodegaId, UUID productoId, BigDecimal cantidadTotal, Instant ultimaActualizacion) {
        this.empresaId = empresaId;
        this.bodegaId = bodegaId;
        this.productoId = productoId;
        this.cantidadTotal = cantidadTotal;
        this.ultimaActualizacion = ultimaActualizacion;
    }

    public UUID getEmpresaId() { return empresaId; }
    public UUID getBodegaId() { return bodegaId; }
    public UUID getProductoId() { return productoId; }
    public BigDecimal getCantidadTotal() { return cantidadTotal; }
    public Instant getUltimaActualizacion() { return ultimaActualizacion; }
    
    public void setCantidadTotal(BigDecimal cantidadTotal) { this.cantidadTotal = cantidadTotal; }
    public void setUltimaActualizacion(Instant ultimaActualizacion) { this.ultimaActualizacion = ultimaActualizacion; }
}
