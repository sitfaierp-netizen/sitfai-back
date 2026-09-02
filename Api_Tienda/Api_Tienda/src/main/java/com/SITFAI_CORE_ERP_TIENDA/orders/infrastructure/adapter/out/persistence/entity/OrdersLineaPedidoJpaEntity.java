package com.SITFAI_CORE_ERP_TIENDA.orders.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Entity(name = "OrdersLineaPedidoJpaEntity")
@Table(name = "orders_linea_pedido")
public class OrdersLineaPedidoJpaEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false)
    private OrdersPedidoJpaEntity pedido;

    @Column(name = "producto_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID productoId;

    @Column(name = "cantidad", nullable = false)
    private int cantidad;

    @Column(name = "precio_unitario", nullable = false, precision = 19, scale = 2)
    private BigDecimal precioUnitario;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public OrdersPedidoJpaEntity getPedido() { return pedido; }
    public void setPedido(OrdersPedidoJpaEntity pedido) { this.pedido = pedido; }

    public UUID getProductoId() { return productoId; }
    public void setProductoId(UUID productoId) { this.productoId = productoId; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }
}
