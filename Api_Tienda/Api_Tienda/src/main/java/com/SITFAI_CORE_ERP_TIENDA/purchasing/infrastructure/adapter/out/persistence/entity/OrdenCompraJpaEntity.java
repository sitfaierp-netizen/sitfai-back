package com.SITFAI_CORE_ERP_TIENDA.purchasing.infrastructure.adapter.out.persistence.entity;

import com.SITFAI_CORE_ERP_TIENDA.core.audit.infrastructure.persistence.entity.AuditableJpaEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad JPA: Representa el registro persistente de una Orden de Compra en {@code purchasing_orden_compra}.
 * <p>
 * Regla MT-01: Aislamiento estricto por {@code empresa_id}.
 * Regla AUD-01: Auditoría completa heredando de {@link AuditableJpaEntity}.
 * Regla CON-01: Concurrencia optimista con {@link Version}.
 */
@Entity
@Table(name = "purchasing_orden_compra", indexes = {
        @Index(name = "idx_purchasing_oc_empresa", columnList = "empresa_id"),
        @Index(name = "idx_purchasing_oc_empresa_prov", columnList = "empresa_id, proveedor_id"),
        @Index(name = "idx_purchasing_oc_empresa_estado", columnList = "empresa_id, estado")
})
public class OrdenCompraJpaEntity extends AuditableJpaEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "empresa_id", length = 36, nullable = false)
    private String empresaId;

    @Column(name = "proveedor_id", length = 36, nullable = false)
    private String proveedorId;

    @Column(name = "estado", length = 50, nullable = false)
    private String estado;

    @Column(name = "costo_total_calculado", precision = 19, scale = 4, nullable = false)
    private BigDecimal costoTotal;

    @Column(name = "emitido_en")
    private Instant emitidoEn;

    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    @OneToMany(mappedBy = "ordenCompra", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LineaOrdenCompraJpaEntity> lineas = new ArrayList<>();

    public OrdenCompraJpaEntity() {}

    public OrdenCompraJpaEntity(String id, String empresaId, String proveedorId, String estado, BigDecimal costoTotal, Long version) {
        this.id = id;
        this.empresaId = empresaId;
        this.proveedorId = proveedorId;
        this.estado = estado;
        this.costoTotal = costoTotal;
        this.version = version != null ? version : 0L;
    }

    public void addLinea(LineaOrdenCompraJpaEntity linea) {
        lineas.add(linea);
        linea.setOrdenCompra(this);
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEmpresaId() { return empresaId; }
    public void setEmpresaId(String empresaId) { this.empresaId = empresaId; }

    public String getProveedorId() { return proveedorId; }
    public void setProveedorId(String proveedorId) { this.proveedorId = proveedorId; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public BigDecimal getCostoTotal() { return costoTotal; }
    public void setCostoTotal(BigDecimal costoTotal) { this.costoTotal = costoTotal; }

    public Instant getEmitidoEn() { return emitidoEn; }
    public void setEmitidoEn(Instant emitidoEn) { this.emitidoEn = emitidoEn; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public List<LineaOrdenCompraJpaEntity> getLineas() { return lineas; }
    public void setLineas(List<LineaOrdenCompraJpaEntity> lineas) { this.lineas = lineas; }
}
