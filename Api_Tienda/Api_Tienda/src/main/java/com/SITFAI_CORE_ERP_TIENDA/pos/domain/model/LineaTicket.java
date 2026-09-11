package com.SITFAI_CORE_ERP_TIENDA.pos.domain.model;

import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.vo.ProductoId;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.valueobject.Dinero;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidad Interna: Línea de un Ticket de Venta POS.
 * Representa un ítem específico (producto, cantidad, precio) dentro de un TicketVenta.
 * Solo se puede crear y acceder a través del Aggregate Root.
 */
public class LineaTicket {

    private final UUID id;
    private final ProductoId productoId;
    private final String descripcionProducto;
    private final BigDecimal cantidad;
    private final Dinero precioUnitario;

    // Constructor protegido: sólo el Aggregate Root puede instanciar
    LineaTicket(ProductoId productoId, String descripcionProducto, BigDecimal cantidad, Dinero precioUnitario) {
        this.id = UUID.randomUUID();
        this.productoId = Objects.requireNonNull(productoId, "ProductoId es obligatorio en LineaTicket");
        this.descripcionProducto = Objects.requireNonNull(descripcionProducto, "La descripción es obligatoria");
        
        Objects.requireNonNull(cantidad, "La cantidad no puede ser nula");
        if (cantidad.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La cantidad de la línea de ticket debe ser mayor a cero");
        }
        this.cantidad = cantidad;
        this.precioUnitario = Objects.requireNonNull(precioUnitario, "El precio unitario es obligatorio");
    }

    /**
     * Calcula el subtotal de esta línea (sin impuestos).
     * MONEY-01: Usa BigDecimal.
     */
    public Dinero calcularSubtotal() {
        return new Dinero(precioUnitario.valor().multiply(cantidad));
    }

    public UUID getId() { return id; }
    public ProductoId getProductoId() { return productoId; }
    public String getDescripcionProducto() { return descripcionProducto; }
    public BigDecimal getCantidad() { return cantidad; }
    public Dinero getPrecioUnitario() { return precioUnitario; }
}
