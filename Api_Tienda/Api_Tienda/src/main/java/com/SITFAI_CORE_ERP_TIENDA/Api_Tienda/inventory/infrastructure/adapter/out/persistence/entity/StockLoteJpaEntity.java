package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidad JPA: Representa un lote específico de stock físico en la base de datos.
 * Tabla: inventory_bodega_lote
 */
@Entity
@Table(name = "inventory_bodega_lote")
public class StockLoteJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false, length = 36)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bodega_id", nullable = false, updatable = false)
    private BodegaJpaEntity bodega;

    @Column(name = "empresa_id", nullable = false, updatable = false, length = 36)
    private UUID empresaId;

    @Column(name = "producto_id", nullable = false, updatable = false, length = 36)
    private UUID productoId;

    @Column(name = "lote_id", nullable = false, updatable = false, length = 100)
    private String loteId;

    @Column(name = "cantidad", nullable = false, precision = 19, scale = 4)
    private BigDecimal cantidad;

    @Column(name = "fecha_caducidad")
    private Instant fechaCaducidad;

    public StockLoteJpaEntity() {
    }

    public StockLoteJpaEntity(UUID id, BodegaJpaEntity bodega, UUID empresaId, UUID productoId, String loteId, BigDecimal cantidad, Instant fechaCaducidad) {
        this.id = id;
        this.bodega = bodega;
        this.empresaId = empresaId;
        this.productoId = productoId;
        this.loteId = loteId;
        this.cantidad = cantidad;
        this.fechaCaducidad = fechaCaducidad;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public BodegaJpaEntity getBodega() {
        return bodega;
    }

    public void setBodega(BodegaJpaEntity bodega) {
        this.bodega = bodega;
    }

    public UUID getEmpresaId() {
        return empresaId;
    }

    public void setEmpresaId(UUID empresaId) {
        this.empresaId = empresaId;
    }

    public UUID getProductoId() {
        return productoId;
    }

    public void setProductoId(UUID productoId) {
        this.productoId = productoId;
    }

    public String getLoteId() {
        return loteId;
    }

    public void setLoteId(String loteId) {
        this.loteId = loteId;
    }

    public BigDecimal getCantidad() {
        return cantidad;
    }

    public void setCantidad(BigDecimal cantidad) {
        this.cantidad = cantidad;
    }

    public Instant getFechaCaducidad() {
        return fechaCaducidad;
    }

    public void setFechaCaducidad(Instant fechaCaducidad) {
        this.fechaCaducidad = fechaCaducidad;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StockLoteJpaEntity that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
