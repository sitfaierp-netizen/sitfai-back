package com.SITFAI_CORE_ERP_TIENDA.billing.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "billing_factura")
public class FacturaJpaEntity {

    @Id
    @Column(name = "id", columnDefinition = "VARCHAR(36)", nullable = false)
    private String id;

    @Column(name = "empresa_id", columnDefinition = "VARCHAR(36)", nullable = false)
    private String empresaId;

    @Column(name = "nit_emisor", nullable = false)
    private String nitEmisor;

    @Column(name = "nit_receptor", nullable = false)
    private String nitReceptor;

    @Column(name = "estado", nullable = false)
    private String estado;

    @Column(name = "cufe")
    private String cufe;

    @Column(name = "resolucion_dian", nullable = false)
    private String resolucionDian; // Para simplificar, guardaremos el prefijo

    @Column(name = "subtotal", precision = 19, scale = 4, nullable = false)
    private BigDecimal subtotal;

    @Column(name = "total_impuestos", precision = 19, scale = 4, nullable = false)
    private BigDecimal totalImpuestos;

    @Column(name = "total_general", precision = 19, scale = 4, nullable = false)
    private BigDecimal totalGeneral;

    @OneToMany(mappedBy = "factura", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LineaFacturaJpaEntity> lineas = new ArrayList<>();

    public FacturaJpaEntity() {}

    // Getters y Setters

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEmpresaId() { return empresaId; }
    public void setEmpresaId(String empresaId) { this.empresaId = empresaId; }

    public String getNitEmisor() { return nitEmisor; }
    public void setNitEmisor(String nitEmisor) { this.nitEmisor = nitEmisor; }

    public String getNitReceptor() { return nitReceptor; }
    public void setNitReceptor(String nitReceptor) { this.nitReceptor = nitReceptor; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getCufe() { return cufe; }
    public void setCufe(String cufe) { this.cufe = cufe; }

    public String getResolucionDian() { return resolucionDian; }
    public void setResolucionDian(String resolucionDian) { this.resolucionDian = resolucionDian; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public BigDecimal getTotalImpuestos() { return totalImpuestos; }
    public void setTotalImpuestos(BigDecimal totalImpuestos) { this.totalImpuestos = totalImpuestos; }

    public BigDecimal getTotalGeneral() { return totalGeneral; }
    public void setTotalGeneral(BigDecimal totalGeneral) { this.totalGeneral = totalGeneral; }

    public List<LineaFacturaJpaEntity> getLineas() { return lineas; }
    public void setLineas(List<LineaFacturaJpaEntity> lineas) { this.lineas = lineas; }
    
    public void addLinea(LineaFacturaJpaEntity linea) {
        lineas.add(linea);
        linea.setFactura(this);
    }
}
