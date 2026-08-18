package com.SITFAI_CORE_ERP_TIENDA.billing.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "billing_nota_credito")
public class NotaCreditoJpaEntity {

    @Id
    @Column(name = "id", columnDefinition = "VARCHAR(36)", nullable = false)
    private String id;

    @Column(name = "empresa_id", columnDefinition = "VARCHAR(36)", nullable = false)
    private String empresaId;

    @Column(name = "factura_afectada_id", columnDefinition = "VARCHAR(36)", nullable = false)
    private String facturaAfectadaId;

    @Column(name = "cufe_factura_afectada", length = 100, nullable = false)
    private String cufeFacturaAfectada;

    @Column(name = "motivo", length = 255, nullable = false)
    private String motivo;

    @Column(name = "estado", length = 30, nullable = false)
    private String estado;

    @Column(name = "cufe", length = 100)
    private String cufe;

    @Column(name = "subtotal", precision = 19, scale = 4, nullable = false)
    private BigDecimal subtotal;

    @Column(name = "total_impuestos", precision = 19, scale = 4, nullable = false)
    private BigDecimal totalImpuestos;

    @Column(name = "total_general", precision = 19, scale = 4, nullable = false)
    private BigDecimal totalGeneral;

    @OneToMany(mappedBy = "notaCredito", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LineaNotaCreditoJpaEntity> lineas = new ArrayList<>();

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getEmpresaId() { return empresaId; }
    public void setEmpresaId(String empresaId) { this.empresaId = empresaId; }
    public String getFacturaAfectadaId() { return facturaAfectadaId; }
    public void setFacturaAfectadaId(String facturaAfectadaId) { this.facturaAfectadaId = facturaAfectadaId; }
    public String getCufeFacturaAfectada() { return cufeFacturaAfectada; }
    public void setCufeFacturaAfectada(String cufeFacturaAfectada) { this.cufeFacturaAfectada = cufeFacturaAfectada; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getCufe() { return cufe; }
    public void setCufe(String cufe) { this.cufe = cufe; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    public BigDecimal getTotalImpuestos() { return totalImpuestos; }
    public void setTotalImpuestos(BigDecimal totalImpuestos) { this.totalImpuestos = totalImpuestos; }
    public BigDecimal getTotalGeneral() { return totalGeneral; }
    public void setTotalGeneral(BigDecimal totalGeneral) { this.totalGeneral = totalGeneral; }
    public List<LineaNotaCreditoJpaEntity> getLineas() { return lineas; }
    public void setLineas(List<LineaNotaCreditoJpaEntity> lineas) { this.lineas = lineas; }
}
