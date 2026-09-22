package com.SITFAI_CORE_ERP_TIENDA.billing.infrastructure.adapter.out.persistence.entity;

import com.SITFAI_CORE_ERP_TIENDA.core.audit.infrastructure.persistence.entity.AuditableJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Entidad JPA: Detalle de línea en la tabla {@code billing_linea_factura}.
 * <p>
 * Regla MT-01: Aislamiento estricto por {@code empresa_id}.
 * Regla AUD-01: Auditoría completa heredando de {@link AuditableJpaEntity}.
 * Regla MONEY-01: Precisión monetaria {@code DECIMAL(19,4)}.
 */
@Entity
@Table(name = "billing_linea_factura")
public class LineaFacturaJpaEntity extends AuditableJpaEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "factura_id", nullable = false)
    private FacturaJpaEntity factura;

    @Column(name = "empresa_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID empresaId;

    @Column(name = "concepto", nullable = false)
    private String concepto;

    @Column(name = "cantidad", nullable = false, precision = 19, scale = 4)
    private BigDecimal cantidad;

    @Column(name = "precio_unitario", nullable = false, precision = 19, scale = 4)
    private BigDecimal precioUnitario;

    @Column(name = "subtotal", nullable = false, precision = 19, scale = 4)
    private BigDecimal subtotal;

    @Column(name = "total_impuestos", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalImpuestos;

    @Column(name = "total", nullable = false, precision = 19, scale = 4)
    private BigDecimal total;

    public LineaFacturaJpaEntity() {}

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public FacturaJpaEntity getFactura() { return factura; }
    public void setFactura(FacturaJpaEntity factura) { this.factura = factura; }

    public UUID getEmpresaId() { return empresaId; }
    public void setEmpresaId(UUID empresaId) { this.empresaId = empresaId; }

    public String getConcepto() { return concepto; }
    public void setConcepto(String concepto) { this.concepto = concepto; }

    public BigDecimal getCantidad() { return cantidad; }
    public void setCantidad(BigDecimal cantidad) { this.cantidad = cantidad; }

    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public BigDecimal getTotalImpuestos() { return totalImpuestos; }
    public void setTotalImpuestos(BigDecimal totalImpuestos) { this.totalImpuestos = totalImpuestos; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
}
