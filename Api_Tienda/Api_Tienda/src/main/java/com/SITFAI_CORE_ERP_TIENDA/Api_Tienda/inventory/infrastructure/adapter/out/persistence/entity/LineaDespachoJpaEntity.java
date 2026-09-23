package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.out.persistence.entity;

import com.SITFAI_CORE_ERP_TIENDA.core.audit.infrastructure.persistence.entity.AuditableJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Entidad JPA para las Líneas de Despacho (Outbound Logistics).
 * <p>
 * Regla AUD-01: Hereda de {@link AuditableJpaEntity}.
 * Regla MT-01: Contiene partición estricta por {@code empresa_id}.
 * Regla MONEY-01: Precisión contable DECIMAL(19,4).
 */
@Entity(name = "InventoryLineaDespachoJpaEntity")
@Table(name = "inventory_linea_despacho")
public class LineaDespachoJpaEntity extends AuditableJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false, length = 36)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "despacho_id", nullable = false, updatable = false)
    private DespachoJpaEntity despacho;

    @Column(name = "empresa_id", nullable = false, updatable = false, length = 36)
    private UUID empresaId;

    @Column(name = "producto_id", nullable = false, updatable = false, length = 36)
    private UUID productoId;

    @Column(name = "cantidad", nullable = false, precision = 19, scale = 4)
    private BigDecimal cantidad;

    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    public LineaDespachoJpaEntity() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public DespachoJpaEntity getDespacho() {
        return despacho;
    }

    public void setDespacho(DespachoJpaEntity despacho) {
        this.despacho = despacho;
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

    public BigDecimal getCantidad() {
        return cantidad;
    }

    public void setCantidad(BigDecimal cantidad) {
        this.cantidad = cantidad;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
