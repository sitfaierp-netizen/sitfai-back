package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.infrastructure.adapter.out.persistence.entity;

import com.SITFAI_CORE_ERP_TIENDA.core.audit.infrastructure.persistence.entity.AuditableJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "orden_produccion")
public class OrdenProduccionJpaEntity extends AuditableJpaEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "empresa_id", nullable = false)
    private UUID empresaId;

    @Column(name = "receta_id", nullable = false)
    private UUID recetaId;

    @Column(name = "bodega_id", nullable = false)
    private UUID bodegaId;

    @Column(name = "cantidad_producir", nullable = false)
    private Integer cantidadProducir;

    @Column(name = "estado", nullable = false)
    private String estado;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getEmpresaId() { return empresaId; }
    public void setEmpresaId(UUID empresaId) { this.empresaId = empresaId; }

    public UUID getRecetaId() { return recetaId; }
    public void setRecetaId(UUID recetaId) { this.recetaId = recetaId; }

    public UUID getBodegaId() { return bodegaId; }
    public void setBodegaId(UUID bodegaId) { this.bodegaId = bodegaId; }

    public Integer getCantidadProducir() { return cantidadProducir; }
    public void setCantidadProducir(Integer cantidadProducir) { this.cantidadProducir = cantidadProducir; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
