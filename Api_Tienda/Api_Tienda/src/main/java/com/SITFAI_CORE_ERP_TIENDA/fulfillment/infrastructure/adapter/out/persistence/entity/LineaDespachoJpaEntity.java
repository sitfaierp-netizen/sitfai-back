package com.SITFAI_CORE_ERP_TIENDA.fulfillment.infrastructure.adapter.out.persistence.entity;

import com.SITFAI_CORE_ERP_TIENDA.core.audit.infrastructure.persistence.entity.AuditableJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "fulfillment_linea_despacho")
public class LineaDespachoJpaEntity extends AuditableJpaEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orden_despacho_id", nullable = false)
    private OrdenDespachoJpaEntity ordenDespacho;

    @Column(name = "producto_id", length = 36, nullable = false, updatable = false)
    private String productoId;

    @Column(name = "cantidad", nullable = false)
    private int cantidad;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public OrdenDespachoJpaEntity getOrdenDespacho() { return ordenDespacho; }
    public void setOrdenDespacho(OrdenDespachoJpaEntity ordenDespacho) { this.ordenDespacho = ordenDespacho; }
    public String getProductoId() { return productoId; }
    public void setProductoId(String productoId) { this.productoId = productoId; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
}
