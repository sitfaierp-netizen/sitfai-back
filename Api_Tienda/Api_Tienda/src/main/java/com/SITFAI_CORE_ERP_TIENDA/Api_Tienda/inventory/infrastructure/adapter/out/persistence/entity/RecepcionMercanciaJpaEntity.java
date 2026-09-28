package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.out.persistence.entity;

import com.SITFAI_CORE_ERP_TIENDA.core.audit.infrastructure.persistence.entity.AuditableJpaEntity;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "recepcion_mercancia")
public class RecepcionMercanciaJpaEntity extends AuditableJpaEntity {
    
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "empresa_id", nullable = false)
    private UUID empresaId;

    @Column(name = "orden_compra_id", nullable = false)
    private UUID ordenCompraId;

    @Column(name = "bodega_id", nullable = false)
    private UUID bodegaId;

    @Column(name = "estado", nullable = false)
    private String estado;

    @OneToMany(mappedBy = "recepcion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LineaRecepcionJpaEntity> lineas = new ArrayList<>();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public UUID getEmpresaId() { return empresaId; }
    public void setEmpresaId(UUID empresaId) { this.empresaId = empresaId; }

    public UUID getOrdenCompraId() { return ordenCompraId; }
    public void setOrdenCompraId(UUID ordenCompraId) { this.ordenCompraId = ordenCompraId; }

    public UUID getBodegaId() { return bodegaId; }
    public void setBodegaId(UUID bodegaId) { this.bodegaId = bodegaId; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public List<LineaRecepcionJpaEntity> getLineas() { return lineas; }
    public void setLineas(List<LineaRecepcionJpaEntity> lineas) { this.lineas = lineas; }
}
