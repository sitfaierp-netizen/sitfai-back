package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.out.persistence.entity;

import com.SITFAI_CORE_ERP_TIENDA.core.audit.infrastructure.persistence.entity.AuditableJpaEntity;
import com.SITFAI_CORE_ERP_TIENDA.core.document.domain.model.enums.DocumentStatus;
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

@Entity
@Table(name = "inventory_recepcion")
public class RecepcionJpaEntity extends AuditableJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false, length = 36)
    private UUID id;

    @Column(name = "empresa_id", nullable = false, updatable = false, length = 36)
    private UUID empresaId;

    @Column(name = "bodega_destino_id", nullable = false, length = 36)
    private UUID bodegaDestinoId;

    @Column(name = "orden_compra_origen_id", nullable = false, length = 36)
    private UUID ordenCompraOrigenId;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    private DocumentStatus estado;

    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    @OneToMany(
            mappedBy = "recepcion",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<LineaRecepcionJpaEntity> lineas = new ArrayList<>();

    public RecepcionJpaEntity() {
    }

    public void agregarLinea(LineaRecepcionJpaEntity linea) {
        this.lineas.add(linea);
        linea.setRecepcion(this);
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

    public UUID getBodegaDestinoId() {
        return bodegaDestinoId;
    }

    public void setBodegaDestinoId(UUID bodegaDestinoId) {
        this.bodegaDestinoId = bodegaDestinoId;
    }

    public UUID getOrdenCompraOrigenId() {
        return ordenCompraOrigenId;
    }

    public void setOrdenCompraOrigenId(UUID ordenCompraOrigenId) {
        this.ordenCompraOrigenId = ordenCompraOrigenId;
    }

    public DocumentStatus getEstado() {
        return estado;
    }

    public void setEstado(DocumentStatus estado) {
        this.estado = estado;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public List<LineaRecepcionJpaEntity> getLineas() {
        return lineas;
    }

    public void setLineas(List<LineaRecepcionJpaEntity> lineas) {
        this.lineas = lineas;
    }
}
