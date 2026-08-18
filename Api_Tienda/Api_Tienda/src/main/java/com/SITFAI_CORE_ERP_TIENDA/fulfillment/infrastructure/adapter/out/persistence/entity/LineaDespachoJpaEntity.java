package com.SITFAI_CORE_ERP_TIENDA.fulfillment.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "fulfillment_linea_despacho")
public class LineaDespachoJpaEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "empresa_id", length = 36, nullable = false)
    private String empresaId;

    @Column(name = "producto_id", length = 36, nullable = false)
    private String productoId;

    @Column(name = "cantidad_solicitada", precision = 19, scale = 4, nullable = false)
    private BigDecimal cantidadSolicitada;

    @Column(name = "cantidad_preparada", precision = 19, scale = 4, nullable = false)
    private BigDecimal cantidadPreparada;

    public LineaDespachoJpaEntity() {
    }

    public LineaDespachoJpaEntity(String id, String empresaId, String productoId, BigDecimal cantidadSolicitada, BigDecimal cantidadPreparada) {
        this.id = id;
        this.empresaId = empresaId;
        this.productoId = productoId;
        this.cantidadSolicitada = cantidadSolicitada;
        this.cantidadPreparada = cantidadPreparada;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmpresaId() {
        return empresaId;
    }

    public void setEmpresaId(String empresaId) {
        this.empresaId = empresaId;
    }

    public String getProductoId() {
        return productoId;
    }

    public void setProductoId(String productoId) {
        this.productoId = productoId;
    }

    public BigDecimal getCantidadSolicitada() {
        return cantidadSolicitada;
    }

    public void setCantidadSolicitada(BigDecimal cantidadSolicitada) {
        this.cantidadSolicitada = cantidadSolicitada;
    }

    public BigDecimal getCantidadPreparada() {
        return cantidadPreparada;
    }

    public void setCantidadPreparada(BigDecimal cantidadPreparada) {
        this.cantidadPreparada = cantidadPreparada;
    }
}
