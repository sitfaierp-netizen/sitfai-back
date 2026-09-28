package com.SITFAI_CORE_ERP_TIENDA.fulfillment.infrastructure.adapter.out.persistence.entity;

import com.SITFAI_CORE_ERP_TIENDA.core.audit.infrastructure.persistence.entity.AuditableJpaEntity;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.model.despacho.vo.EstadoDespacho;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "fulfillment_orden_despacho")
public class OrdenDespachoJpaEntity extends AuditableJpaEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id;

    @Column(name = "empresa_id", length = 36, nullable = false, updatable = false)
    private String empresaId;

    @Column(name = "pedido_id", length = 36, nullable = false, updatable = false)
    private String pedidoId;

    @Column(name = "bodega_id", length = 36, nullable = false, updatable = false)
    private String bodegaId;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", length = 30, nullable = false)
    private EstadoDespacho estado;

    @OneToMany(mappedBy = "ordenDespacho", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LineaDespachoJpaEntity> lineas = new ArrayList<>();

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getEmpresaId() { return empresaId; }
    public void setEmpresaId(String empresaId) { this.empresaId = empresaId; }
    public String getPedidoId() { return pedidoId; }
    public void setPedidoId(String pedidoId) { this.pedidoId = pedidoId; }
    public String getBodegaId() { return bodegaId; }
    public void setBodegaId(String bodegaId) { this.bodegaId = bodegaId; }
    public EstadoDespacho getEstado() { return estado; }
    public void setEstado(EstadoDespacho estado) { this.estado = estado; }
    public List<LineaDespachoJpaEntity> getLineas() { return lineas; }
    public void setLineas(List<LineaDespachoJpaEntity> lineas) { this.lineas = lineas; }
}
