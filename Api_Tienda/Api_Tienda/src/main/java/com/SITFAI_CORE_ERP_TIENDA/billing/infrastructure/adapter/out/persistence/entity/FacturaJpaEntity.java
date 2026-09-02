package com.SITFAI_CORE_ERP_TIENDA.billing.infrastructure.adapter.out.persistence.entity;

import com.SITFAI_CORE_ERP_TIENDA.core.audit.infrastructure.persistence.entity.AuditableJpaEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "billing_factura", uniqueConstraints = {
        @UniqueConstraint(name = "uq_billing_factura_empresa_id_uuid", columnNames = {"empresa_id", "id"})
})
public class FacturaJpaEntity extends AuditableJpaEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "empresa_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID empresaId;

    @Column(name = "cliente_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID clienteId;

    @Column(name = "pedido_id", columnDefinition = "BINARY(16)")
    private UUID pedidoId;

    @Column(name = "ruc_cliente", nullable = false, length = 13)
    private String rucCliente;

    @Column(name = "subtotal", nullable = false, precision = 19, scale = 4)
    private BigDecimal subtotal;

    @Column(name = "total_impuestos", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalImpuestos;

    @Column(name = "total_general", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalGeneral;

    @Column(name = "estado", nullable = false, length = 20)
    private String estado;

    @Version
    @Column(name = "version")
    private Long version;

    @OneToMany(mappedBy = "factura", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LineaFacturaJpaEntity> lineas = new ArrayList<>();

    public FacturaJpaEntity() {}

    public void addLinea(LineaFacturaJpaEntity linea) {
        lineas.add(linea);
        linea.setFactura(this);
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getEmpresaId() { return empresaId; }
    public void setEmpresaId(UUID empresaId) { this.empresaId = empresaId; }
    public UUID getClienteId() { return clienteId; }
    public void setClienteId(UUID clienteId) { this.clienteId = clienteId; }
    public UUID getPedidoId() { return pedidoId; }
    public void setPedidoId(UUID pedidoId) { this.pedidoId = pedidoId; }
    public String getRucCliente() { return rucCliente; }
    public void setRucCliente(String rucCliente) { this.rucCliente = rucCliente; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    public BigDecimal getTotalImpuestos() { return totalImpuestos; }
    public void setTotalImpuestos(BigDecimal totalImpuestos) { this.totalImpuestos = totalImpuestos; }
    public BigDecimal getTotalGeneral() { return totalGeneral; }
    public void setTotalGeneral(BigDecimal totalGeneral) { this.totalGeneral = totalGeneral; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
    public List<LineaFacturaJpaEntity> getLineas() { return lineas; }
    public void setLineas(List<LineaFacturaJpaEntity> lineas) { this.lineas = lineas; }
}
