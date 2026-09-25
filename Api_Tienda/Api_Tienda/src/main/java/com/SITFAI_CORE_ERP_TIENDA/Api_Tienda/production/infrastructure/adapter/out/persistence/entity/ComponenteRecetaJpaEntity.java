package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.infrastructure.adapter.out.persistence.entity;

import com.SITFAI_CORE_ERP_TIENDA.core.audit.infrastructure.persistence.entity.AuditableJpaEntity;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "componente_receta")
public class ComponenteRecetaJpaEntity extends AuditableJpaEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receta_id", nullable = false)
    private ListaMaterialesJpaEntity listaMateriales;

    @Column(name = "insumo_id", nullable = false)
    private UUID insumoId;

    @Column(name = "cantidad", nullable = false, precision = 19, scale = 4)
    private BigDecimal cantidad;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public ListaMaterialesJpaEntity getListaMateriales() { return listaMateriales; }
    public void setListaMateriales(ListaMaterialesJpaEntity listaMateriales) { this.listaMateriales = listaMateriales; }

    public UUID getInsumoId() { return insumoId; }
    public void setInsumoId(UUID insumoId) { this.insumoId = insumoId; }

    public BigDecimal getCantidad() { return cantidad; }
    public void setCantidad(BigDecimal cantidad) { this.cantidad = cantidad; }
}
