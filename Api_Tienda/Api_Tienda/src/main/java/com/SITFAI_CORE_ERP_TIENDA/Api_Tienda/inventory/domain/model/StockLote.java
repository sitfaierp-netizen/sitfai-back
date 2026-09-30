package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.LoteId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.ProductoId;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * Entidad interna del Agregado Bodega para gestionar la trazabilidad FEFO.
 */
public class StockLote {
    private final ProductoId productoId;
    private final LoteId loteId;
    private BigDecimal cantidad; // Stock disponible
    private BigDecimal cantidadReservada; // Stock reservado
    private final Instant fechaCaducidad;

    public StockLote(ProductoId productoId, LoteId loteId, BigDecimal cantidadInicial, Instant fechaCaducidad) {
        this(productoId, loteId, cantidadInicial, BigDecimal.ZERO, fechaCaducidad);
    }

    public StockLote(ProductoId productoId, LoteId loteId, BigDecimal cantidadInicial,
                     BigDecimal cantidadReservada, Instant fechaCaducidad) {
        this.productoId = Objects.requireNonNull(productoId, "StockLote: productoId es obligatorio.");
        this.loteId = Objects.requireNonNull(loteId, "StockLote: loteId es obligatorio.");
        this.cantidad = Objects.requireNonNull(cantidadInicial, "StockLote: cantidadInicial es obligatoria.");
        this.cantidadReservada = Objects.requireNonNull(cantidadReservada, "StockLote: cantidadReservada es obligatoria.");
        if (this.cantidad.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("StockLote: La cantidad inicial no puede ser negativa.");
        }
        if (this.cantidadReservada.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("StockLote: La cantidad reservada no puede ser negativa.");
        }
        this.fechaCaducidad = fechaCaducidad;
    }

    public void agregar(BigDecimal cant) {
        if (cant == null || cant.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("StockLote: La cantidad a agregar debe ser mayor a cero.");
        }
        this.cantidad = this.cantidad.add(cant);
    }

    public void descontar(BigDecimal cant) {
        if (cant == null || cant.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("StockLote: La cantidad a descontar debe ser mayor a cero.");
        }
        if (this.cantidad.compareTo(cant) < 0) {
            throw new IllegalArgumentException("StockLote: No hay stock suficiente en este lote para descontar.");
        }
        this.cantidad = this.cantidad.subtract(cant);
    }

    public ProductoId getProductoId() { return productoId; }
    public LoteId getLoteId() { return loteId; }
    public BigDecimal getCantidad() { return cantidad; }
    public BigDecimal getCantidadReservada() { return cantidadReservada; }
    public Instant getFechaCaducidad() { return fechaCaducidad; }

    public void reservar(BigDecimal cant) {
        if (cant == null || cant.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("StockLote: La cantidad a reservar debe ser mayor a cero.");
        }
        if (this.cantidad.compareTo(cant) < 0) {
            throw new IllegalArgumentException("StockLote: No hay stock disponible suficiente en este lote para reservar.");
        }
        this.cantidad = this.cantidad.subtract(cant);
        this.cantidadReservada = this.cantidadReservada.add(cant);
    }

    public void descontarReservado(BigDecimal cant) {
        if (cant == null || cant.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("StockLote: La cantidad a descontar de la reserva debe ser mayor a cero.");
        }
        if (this.cantidadReservada.compareTo(cant) < 0) {
            throw new IllegalArgumentException("StockLote: No hay stock reservado suficiente en este lote para descontar.");
        }
        this.cantidadReservada = this.cantidadReservada.subtract(cant);
    }
}
