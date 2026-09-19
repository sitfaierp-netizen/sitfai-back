package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.out.persistence.jpa.entity.ajuste;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "inv_lineas_ajuste")
public class LineaAjusteJpaEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ajuste_id", nullable = false)
    private AjusteInventarioJpaEntity ajuste;

    @Column(name = "producto_id", nullable = false)
    private UUID productoId;

    @Column(name = "codigo_lote", length = 100)
    private String codigoLote;

    @Column(name = "fecha_caducidad")
    private LocalDate fechaCaducidad;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal diferencia;

    public LineaAjusteJpaEntity() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public AjusteInventarioJpaEntity getAjuste() { return ajuste; }
    public void setAjuste(AjusteInventarioJpaEntity ajuste) { this.ajuste = ajuste; }

    public UUID getProductoId() { return productoId; }
    public void setProductoId(UUID productoId) { this.productoId = productoId; }

    public String getCodigoLote() { return codigoLote; }
    public void setCodigoLote(String codigoLote) { this.codigoLote = codigoLote; }

    public LocalDate getFechaCaducidad() { return fechaCaducidad; }
    public void setFechaCaducidad(LocalDate fechaCaducidad) { this.fechaCaducidad = fechaCaducidad; }

    public BigDecimal getDiferencia() { return diferencia; }
    public void setDiferencia(BigDecimal diferencia) { this.diferencia = diferencia; }
}
