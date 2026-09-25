package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.infrastructure.adapter.out.persistence.entity;

import com.SITFAI_CORE_ERP_TIENDA.core.audit.infrastructure.persistence.entity.AuditableJpaEntity;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "lista_materiales")
public class ListaMaterialesJpaEntity extends AuditableJpaEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "empresa_id", nullable = false)
    private UUID empresaId;

    @Column(name = "producto_final_id", nullable = false)
    private UUID productoFinalId;

    @Column(name = "estado", nullable = false)
    private String estado;

    @OneToMany(mappedBy = "listaMateriales", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ComponenteRecetaJpaEntity> componentes = new ArrayList<>();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getEmpresaId() { return empresaId; }
    public void setEmpresaId(UUID empresaId) { this.empresaId = empresaId; }

    public UUID getProductoFinalId() { return productoFinalId; }
    public void setProductoFinalId(UUID productoFinalId) { this.productoFinalId = productoFinalId; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public List<ComponenteRecetaJpaEntity> getComponentes() { return componentes; }
    public void setComponentes(List<ComponenteRecetaJpaEntity> componentes) { this.componentes = componentes; }
}
