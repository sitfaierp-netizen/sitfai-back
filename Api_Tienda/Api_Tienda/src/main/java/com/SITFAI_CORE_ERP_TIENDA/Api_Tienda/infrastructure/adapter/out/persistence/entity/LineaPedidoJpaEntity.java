package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidad JPA: Representa la tabla 'tienda_linea_pedido'.
 * <p>
 * Pertenece exclusivamente a la Capa de Infraestructura (REGLA-1, REGLA-6).
 * Incluye {@code empresaId} para garantizar aislamiento multitenant a nivel de fila (MT-01, MT-03).
 */
@Entity
@Table(name = "tienda_linea_pedido")
public class LineaPedidoJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false, length = 36)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false)
    private PedidoJpaEntity pedido;

    @Column(name = "empresa_id", nullable = false, updatable = false, length = 36)
    private UUID empresaId;

    @Column(name = "producto_id", nullable = false, length = 36)
    private UUID productoId;

    @Column(name = "cantidad", nullable = false)
    private int cantidad;

    @Column(name = "precio_unitario", nullable = false, precision = 19, scale = 4)
    private BigDecimal precioUnitario;

    @Column(name = "moneda", nullable = false, length = 3)
    private String moneda;

    @Column(name = "subtotal", nullable = false, precision = 19, scale = 4)
    private BigDecimal subtotal;

    public LineaPedidoJpaEntity() {
    }

    public LineaPedidoJpaEntity(
            UUID id,
            PedidoJpaEntity pedido,
            UUID empresaId,
            UUID productoId,
            int cantidad,
            BigDecimal precioUnitario,
            String moneda,
            BigDecimal subtotal) {
        this.id = id;
        this.pedido = pedido;
        this.empresaId = empresaId;
        this.productoId = productoId;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.moneda = moneda;
        this.subtotal = subtotal;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public PedidoJpaEntity getPedido() {
        return pedido;
    }

    public void setPedido(PedidoJpaEntity pedido) {
        this.pedido = pedido;
    }

    public UUID getEmpresaId() {
        return empresaId;
    }

    public void setEmpresaId(UUID empresaId) {
        this.empresaId = empresaId;
    }

    public UUID getProductoId() {
        return productoId;
    }

    public void setProductoId(UUID productoId) {
        this.productoId = productoId;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public String getMoneda() {
        return moneda;
    }

    public void setMoneda(String moneda) {
        this.moneda = moneda;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LineaPedidoJpaEntity that = (LineaPedidoJpaEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
