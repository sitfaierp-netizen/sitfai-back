package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.out.persistence.entity;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.TipoMovimiento;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
 * Entidad JPA: Representa la tabla 'inventory_movimiento'.
 * <p>
 * Pertenece exclusivamente a la Capa de Infraestructura (REGLA-1, REGLA-6).
 * Almacena el historial inmutable de movimientos de stock con su documento fuente (BOD-04).
 * <p>
 * Reglas validadas:
 * <ul>
 *   <li>REGLA-6: Entidades JPA en infrastructure/adapter/out/persistence.</li>
 *   <li>MT-01 / MT-03: Columna {@code empresa_id} para aislamiento multitenant.</li>
 *   <li>BOD-03 / BOD-04: Registro atado a bodega y documento fuente.</li>
 * </ul>
 */
@Entity
@Table(name = "inventory_movimiento")
public class MovimientoInventarioJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false, length = 36)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bodega_id", nullable = false, updatable = false)
    private BodegaJpaEntity bodega;

    @Column(name = "producto_id", nullable = false, updatable = false, length = 36)
    private UUID productoId;

    @Column(name = "empresa_id", nullable = false, updatable = false, length = 36)
    private UUID empresaId;

    @Column(name = "cantidad", nullable = false, updatable = false, precision = 19, scale = 4)
    private BigDecimal cantidad;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, updatable = false, length = 20)
    private TipoMovimiento tipo;

    @Column(name = "lote_id", length = 100)
    private String loteId;

    @Column(name = "doc_fuente_tipo", nullable = false, updatable = false, length = 50)
    private String docFuenteTipo;

    @Column(name = "doc_fuente_numero", nullable = false, updatable = false, length = 100)
    private String docFuenteNumero;

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private Instant fechaRegistro;

    public MovimientoInventarioJpaEntity() {
        // Constructor vacío requerido por JPA
    }

    public MovimientoInventarioJpaEntity(
            UUID id,
            BodegaJpaEntity bodega,
            UUID productoId,
            UUID empresaId,
            BigDecimal cantidad,
            TipoMovimiento tipo,
            String loteId,
            String docFuenteTipo,
            String docFuenteNumero,
            Instant fechaRegistro) {
        this.id = id;
        this.bodega = bodega;
        this.productoId = productoId;
        this.empresaId = empresaId;
        this.cantidad = cantidad;
        this.tipo = tipo;
        this.loteId = loteId;
        this.docFuenteTipo = docFuenteTipo;
        this.docFuenteNumero = docFuenteNumero;
        this.fechaRegistro = fechaRegistro;
    }

    // Getters y Setters

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

    public UUID getProductoId() {
        return productoId;
    }

    public void setProductoId(UUID productoId) {
        this.productoId = productoId;
    }

    public UUID getEmpresaId() {
        return empresaId;
    }

    public void setEmpresaId(UUID empresaId) {
        this.empresaId = empresaId;
    }

    public BigDecimal getCantidad() {
        return cantidad;
    }

    public void setCantidad(BigDecimal cantidad) {
        this.cantidad = cantidad;
    }

    public TipoMovimiento getTipo() {
        return tipo;
    }

    public void setTipo(TipoMovimiento tipo) {
        this.tipo = tipo;
    }

    public String getLoteId() {
        return loteId;
    }

    public void setLoteId(String loteId) {
        this.loteId = loteId;
    }

    public String getDocFuenteTipo() {
        return docFuenteTipo;
    }

    public void setDocFuenteTipo(String docFuenteTipo) {
        this.docFuenteTipo = docFuenteTipo;
    }

    public String getDocFuenteNumero() {
        return docFuenteNumero;
    }

    public void setDocFuenteNumero(String docFuenteNumero) {
        this.docFuenteNumero = docFuenteNumero;
    }

    public Instant getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(Instant fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MovimientoInventarioJpaEntity that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
