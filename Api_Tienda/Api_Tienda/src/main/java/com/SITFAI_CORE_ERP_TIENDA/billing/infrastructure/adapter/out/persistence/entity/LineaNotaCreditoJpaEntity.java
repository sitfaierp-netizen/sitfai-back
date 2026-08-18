package com.SITFAI_CORE_ERP_TIENDA.billing.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "billing_linea_nota_credito")
public class LineaNotaCreditoJpaEntity {

    @Id
    @Column(name = "id", columnDefinition = "VARCHAR(36)", nullable = false)
    private String id;

    @Column(name = "empresa_id", columnDefinition = "VARCHAR(36)", nullable = false)
    private String empresaId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nota_credito_id", nullable = false)
    private NotaCreditoJpaEntity notaCredito;

    @Column(name = "concepto", length = 255, nullable = false)
    private String concepto;

    @Column(name = "cantidad", precision = 19, scale = 4, nullable = false)
    private BigDecimal cantidad;

    @Column(name = "precio_unitario", precision = 19, scale = 4, nullable = false)
    private BigDecimal precioUnitario;

    @Column(name = "subtotal", precision = 19, scale = 4, nullable = false)
    private BigDecimal subtotal;

    @Column(name = "total_impuestos", precision = 19, scale = 4, nullable = false)
    private BigDecimal totalImpuestos;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getEmpresaId() { return empresaId; }
    public void setEmpresaId(String empresaId) { this.empresaId = empresaId; }
    public NotaCreditoJpaEntity getNotaCredito() { return notaCredito; }
    public void setNotaCredito(NotaCreditoJpaEntity notaCredito) { this.notaCredito = notaCredito; }
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
}
