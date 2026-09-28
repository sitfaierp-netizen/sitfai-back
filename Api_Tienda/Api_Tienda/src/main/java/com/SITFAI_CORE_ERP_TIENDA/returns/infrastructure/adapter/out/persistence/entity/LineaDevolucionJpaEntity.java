package com.SITFAI_CORE_ERP_TIENDA.returns.infrastructure.adapter.out.persistence.entity;

import com.SITFAI_CORE_ERP_TIENDA.core.audit.infrastructure.persistence.entity.AuditableJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "returns_linea_devolucion")
public class LineaDevolucionJpaEntity extends AuditableJpaEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "autorizacion_devolucion_id", nullable = false)
    private AutorizacionDevolucionJpaEntity autorizacionDevolucion;

    @Column(name = "producto_id", length = 36, nullable = false, updatable = false)
    private String productoId;

    @Column(name = "cantidad", nullable = false)
    private int cantidad;

    @Column(name = "motivo", nullable = false)
    private String motivo;

    @Column(name = "estado_inspeccion", length = 20, nullable = false)
    private String estadoInspeccion;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public AutorizacionDevolucionJpaEntity getAutorizacionDevolucion() { return autorizacionDevolucion; }
    public void setAutorizacionDevolucion(AutorizacionDevolucionJpaEntity autorizacionDevolucion) { this.autorizacionDevolucion = autorizacionDevolucion; }
    public String getProductoId() { return productoId; }
    public void setProductoId(String productoId) { this.productoId = productoId; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public String getEstadoInspeccion() { return estadoInspeccion; }
    public void setEstadoInspeccion(String estadoInspeccion) { this.estadoInspeccion = estadoInspeccion; }
}
