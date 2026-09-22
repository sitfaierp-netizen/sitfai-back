package com.SITFAI_CORE_ERP_TIENDA.billing.infrastructure.adapter.out.persistence.entity;

import com.SITFAI_CORE_ERP_TIENDA.core.audit.infrastructure.persistence.entity.AuditableJpaEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entidad JPA: Representa el registro persistente de una Factura en la tabla {@code billing_factura}.
 * <p>
 * Regla MT-01: Aislamiento estricto por {@code empresa_id}.
 * Regla AUD-01: Auditoría completa heredando de {@link AuditableJpaEntity}.
 * Regla CON-01: Concurrencia optimista con {@link Version}.
 */
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

    @Column(name = "tipo_origen", length = 30)
    private String tipoOrigen;

    @Column(name = "documento_fuente_id", columnDefinition = "BINARY(16)")
    private UUID documentoFuenteId;

    @Column(name = "ruc_cliente", length = 30)
    private String rucCliente;

    @Column(name = "subtotal", nullable = false, precision = 19, scale = 4)
    private BigDecimal subtotal;

    @Column(name = "total_impuestos", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalImpuestos;

    @Column(name = "total_general", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalGeneral;

    @Column(name = "estado", nullable = false, length = 20)
    private String estado;

    @Column(name = "motivo_anulacion", length = 255)
    private String motivoAnulacion;

    @Column(name = "anulado_en")
    private Instant anuladoEn;

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

    public String getTipoOrigen() { return tipoOrigen; }
    public void setTipoOrigen(String tipoOrigen) { this.tipoOrigen = tipoOrigen; }

    public UUID getDocumentoFuenteId() { return documentoFuenteId; }
    public void setDocumentoFuenteId(UUID documentoFuenteId) { this.documentoFuenteId = documentoFuenteId; }

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

    public String getMotivoAnulacion() { return motivoAnulacion; }
    public void setMotivoAnulacion(String motivoAnulacion) { this.motivoAnulacion = motivoAnulacion; }

    public Instant getAnuladoEn() { return anuladoEn; }
    public void setAnuladoEn(Instant anuladoEn) { this.anuladoEn = anuladoEn; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public List<LineaFacturaJpaEntity> getLineas() { return lineas; }
    public void setLineas(List<LineaFacturaJpaEntity> lineas) { this.lineas = lineas; }
}
