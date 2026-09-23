package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.out.persistence.entity;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.despacho.vo.EstadoDespacho;
import com.SITFAI_CORE_ERP_TIENDA.core.audit.infrastructure.persistence.entity.AuditableJpaEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entidad JPA para el Despacho de Inventario (Outbound Logistics).
 * <p>
 * Regla AUD-01: Hereda de {@link AuditableJpaEntity} para trazabilidad obligatoria.
 * Regla MT-01: Partición estricta mediante {@code empresa_id}.
 */
@Entity
@Table(name = "inventory_despacho")
public class DespachoJpaEntity extends AuditableJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false, length = 36)
    private UUID id;

    @Column(name = "empresa_id", nullable = false, updatable = false, length = 36)
    private UUID empresaId;

    @Column(name = "pedido_id", nullable = false, updatable = false, length = 36)
    private UUID pedidoId;

    @Column(name = "bodega_id", length = 36)
    private UUID bodegaId;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    private EstadoDespacho estado;

    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    @OneToMany(
            mappedBy = "despacho",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<LineaDespachoJpaEntity> lineas = new ArrayList<>();

    public DespachoJpaEntity() {
    }

    public void agregarLinea(LineaDespachoJpaEntity linea) {
        this.lineas.add(linea);
        linea.setDespacho(this);
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getEmpresaId() {
        return empresaId;
    }

    public void setEmpresaId(UUID empresaId) {
        this.empresaId = empresaId;
    }

    public UUID getPedidoId() {
        return pedidoId;
    }

    public void setPedidoId(UUID pedidoId) {
        this.pedidoId = pedidoId;
    }

    public UUID getBodegaId() {
        return bodegaId;
    }

    public void setBodegaId(UUID bodegaId) {
        this.bodegaId = bodegaId;
    }

    public EstadoDespacho getEstado() {
        return estado;
    }

    public void setEstado(EstadoDespacho estado) {
        this.estado = estado;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public List<LineaDespachoJpaEntity> getLineas() {
        return lineas;
    }

    public void setLineas(List<LineaDespachoJpaEntity> lineas) {
        this.lineas = lineas;
    }
}
