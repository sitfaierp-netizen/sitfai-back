package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.exception;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.BodegaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.ProductoId;

import java.math.BigDecimal;

/**
 * Excepción de Dominio que se lanza cuando se viola la invariante BOD-05:
 * "El stock en una Bodega nunca puede ser negativo."
 * <p>
 * El Agregado {@code Bodega} lanza esta excepción dentro de
 * {@code registrarMovimiento()} cuando un movimiento de SALIDA dejaría
 * el stock de un producto en valor negativo.
 * <p>
 * Contiene contexto rico para diagnóstico: bodegaId, productoId,
 * stock disponible actual y cantidad solicitada.
 * <p>
 * Reglas validadas: BOD-05 (invariante), REGLA-1 (cero frameworks), MCP-01.
 */
public final class StockInsuficienteException extends DomainException {

    private static final String CODIGO_ERROR = "BOD-05";

    private final BodegaId bodegaId;
    private final ProductoId productoId;
    private final BigDecimal stockDisponible;
    private final BigDecimal cantidadSolicitada;

    public StockInsuficienteException(
            BodegaId bodegaId,
            ProductoId productoId,
            BigDecimal stockDisponible,
            BigDecimal cantidadSolicitada) {

        super(CODIGO_ERROR, construirMensaje(bodegaId, productoId, stockDisponible, cantidadSolicitada));
        this.bodegaId = bodegaId;
        this.productoId = productoId;
        this.stockDisponible = stockDisponible;
        this.cantidadSolicitada = cantidadSolicitada;
    }

    private static String construirMensaje(
            BodegaId bodegaId,
            ProductoId productoId,
            BigDecimal stockDisponible,
            BigDecimal cantidadSolicitada) {

        return String.format(
                "[BOD-05] Stock insuficiente en Bodega '%s' para Producto '%s'. " +
                "Stock disponible: %s | Cantidad solicitada: %s.",
                bodegaId,
                productoId,
                stockDisponible.toPlainString(),
                cantidadSolicitada.toPlainString()
        );
    }

    public BodegaId getBodegaId() {
        return bodegaId;
    }

    public ProductoId getProductoId() {
        return productoId;
    }

    public BigDecimal getStockDisponible() {
        return stockDisponible;
    }

    public BigDecimal getCantidadSolicitada() {
        return cantidadSolicitada;
    }
}
