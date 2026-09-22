package com.SITFAI_CORE_ERP_TIENDA.purchasing.infrastructure.adapter.out.persistence.entity;

import com.SITFAI_CORE_ERP_TIENDA.core.audit.infrastructure.persistence.entity.AuditableJpaEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Entidad JPA: Detalle de línea en la tabla {@code purchasing_linea_orden}.
 * <p>
 * Regla MT-01: Aislamiento estricto por {@code empresa_id}.
 * Regla AUD-01: Auditoría completa heredando de {@link AuditableJpaEntity}.
 * Regla MONEY-01: Precisión monetaria {@code DECIMAL(19,4)}.
 */
@Entity
@Table(name = "purchasing_linea_orden", indexes = {
        @Index(name = "idx_purchasing_lo_empresa", columnList = "empresa_id")
})
public class LineaOrdenCompraJpaEntity extends AuditableJpaEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orden_compra_id", nullable = false)
    private OrdenCompraJpaEntity ordenCompra;

    @Column(name = "empresa_id", length = 36, nullable = false)
    private String empresaId;

    @Column(name = "producto_id", length = 36, nullable = false)
    private String productoId;

    @Column(name = "cantidad_solicitada", precision = 19, scale = 4, nullable = false)
    private BigDecimal cantidadSolicitada;

    @Column(name = "costo_unitario_esperado", precision = 19, scale = 4, nullable = false)
    private BigDecimal costoUnitarioEsperado;

    @Column(name = "subtotal", precision = 19, scale = 4, nullable = false)
    private BigDecimal subtotal;

    public LineaOrdenCompraJpaEntity() {}

    public LineaOrdenCompraJpaEntity(String empresaId, String productoId, BigDecimal cantidadSolicitada, BigDecimal costoUnitarioEsperado, BigDecimal subtotal) {
        this.id = UUID.randomUUID().toString();
        this.empresaId = empresaId;
        this.productoId = productoId;
        this.cantidadSolicitada = cantidadSolicitada;
        this.costoUnitarioEsperado = costoUnitarioEsperado;
        this.subtotal = subtotal;
    }

    public LineaOrdenCompraJpaEntity(String id, String empresaId, String productoId, BigDecimal cantidadSolicitada, BigDecimal costoUnitarioEsperado, BigDecimal subtotal) {
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.empresaId = empresaId;
        this.productoId = productoId;
        this.cantidadSolicitada = cantidadSolicitada;
        this.costoUnitarioEsperado = costoUnitarioEsperado;
        this.subtotal = subtotal;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public OrdenCompraJpaEntity getOrdenCompra() { return ordenCompra; }
    public void setOrdenCompra(OrdenCompraJpaEntity ordenCompra) { this.ordenCompra = ordenCompra; }
    public String getEmpresaId() { return empresaId; }
    public void setEmpresaId(String empresaId) { this.empresaId = empresaId; }
    public String getProductoId() { return productoId; }
    public void setProductoId(String productoId) { this.productoId = productoId; }
    public BigDecimal getCantidadSolicitada() { return cantidadSolicitada; }
    public void setCantidadSolicitada(BigDecimal cantidadSolicitada) { this.cantidadSolicitada = cantidadSolicitada; }
    public BigDecimal getCostoUnitarioEsperado() { return costoUnitarioEsperado; }
    public void setCostoUnitarioEsperado(BigDecimal costoUnitarioEsperado) { this.costoUnitarioEsperado = costoUnitarioEsperado; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
}
