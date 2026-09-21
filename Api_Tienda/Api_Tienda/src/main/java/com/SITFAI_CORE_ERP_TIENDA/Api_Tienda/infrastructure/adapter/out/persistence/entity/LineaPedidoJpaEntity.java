package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.infrastructure.adapter.out.persistence.entity;

import com.SITFAI_CORE_ERP_TIENDA.core.audit.infrastructure.persistence.entity.AuditableJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;

@Entity
@Table(name = "tienda_linea_pedido")
public class LineaPedidoJpaEntity extends AuditableJpaEntity {

    @Id
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false)
    private PedidoJpaEntity pedido;

    @Column(name = "empresa_id", nullable = false)
    private String empresaId;

    @Column(name = "producto_id", nullable = false)
    private String productoId;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    @Column(name = "precio_unitario", nullable = false)
    private BigDecimal precioUnitario;

    @Column(name = "moneda", nullable = false)
    private String moneda;

    @Column(name = "subtotal", nullable = false)
    private BigDecimal subtotal;

    @Version
    @Column(name = "version", nullable = false)
    private Integer version;

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public PedidoJpaEntity getPedido() { return pedido; }
    public void setPedido(PedidoJpaEntity pedido) { this.pedido = pedido; }
    public String getEmpresaId() { return empresaId; }
    public void setEmpresaId(String empresaId) { this.empresaId = empresaId; }
    public String getProductoId() { return productoId; }
    public void setProductoId(String productoId) { this.productoId = productoId; }
    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }
    public String getMoneda() { return moneda; }
    public void setMoneda(String moneda) { this.moneda = moneda; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
}
