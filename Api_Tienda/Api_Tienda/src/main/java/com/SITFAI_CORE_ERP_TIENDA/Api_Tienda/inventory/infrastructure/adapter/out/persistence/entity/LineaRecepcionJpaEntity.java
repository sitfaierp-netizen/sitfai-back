package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.out.persistence.entity;

import com.SITFAI_CORE_ERP_TIENDA.core.audit.infrastructure.persistence.entity.AuditableJpaEntity;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "linea_recepcion_mercancia")
public class LineaRecepcionJpaEntity extends AuditableJpaEntity {
    
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recepcion_id", nullable = false)
    private RecepcionMercanciaJpaEntity recepcion;

    @Column(name = "producto_id", nullable = false)
    private UUID productoId;

    @Column(name = "cantidad_esperada", nullable = false)
    private int cantidadEsperada;

    @Column(name = "cantidad_recibida", nullable = false)
    private int cantidadRecibida;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public RecepcionMercanciaJpaEntity getRecepcion() { return recepcion; }
    public void setRecepcion(RecepcionMercanciaJpaEntity recepcion) { this.recepcion = recepcion; }

    public UUID getProductoId() { return productoId; }
    public void setProductoId(UUID productoId) { this.productoId = productoId; }

    public int getCantidadEsperada() { return cantidadEsperada; }
    public void setCantidadEsperada(int cantidadEsperada) { this.cantidadEsperada = cantidadEsperada; }

    public int getCantidadRecibida() { return cantidadRecibida; }
    public void setCantidadRecibida(int cantidadRecibida) { this.cantidadRecibida = cantidadRecibida; }
}
