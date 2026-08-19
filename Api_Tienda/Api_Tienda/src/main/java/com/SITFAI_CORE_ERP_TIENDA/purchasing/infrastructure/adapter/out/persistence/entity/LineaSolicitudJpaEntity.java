package com.SITFAI_CORE_ERP_TIENDA.purchasing.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

/**
 * JPA Entity: purchasing_linea_solicitud.
 * Pertenece al Agregado de infraestructura de SolicitudJpaEntity.
 */
@Entity
@Table(name = "purchasing_linea_solicitud")
public class LineaSolicitudJpaEntity {

    @Id
    @Column(name = "id", nullable = false, length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solicitud_id", nullable = false)
    private SolicitudJpaEntity solicitud;

    @Column(name = "empresa_id", nullable = false, length = 36)
    private String empresaId;

    @Column(name = "producto_id", nullable = false, length = 36)
    private String productoId;

    @Column(name = "cantidad_solicitada", nullable = false, precision = 19, scale = 4)
    private BigDecimal cantidadSolicitada;

    protected LineaSolicitudJpaEntity() {}

    public LineaSolicitudJpaEntity(String id, SolicitudJpaEntity solicitud,
                                    String empresaId, String productoId,
                                    BigDecimal cantidadSolicitada) {
        this.id                 = id;
        this.solicitud          = solicitud;
        this.empresaId          = empresaId;
        this.productoId         = productoId;
        this.cantidadSolicitada = cantidadSolicitada;
    }

    public String getId()                      { return id; }
    public SolicitudJpaEntity getSolicitud()   { return solicitud; }
    public String getEmpresaId()               { return empresaId; }
    public String getProductoId()              { return productoId; }
    public BigDecimal getCantidadSolicitada()  { return cantidadSolicitada; }
}
