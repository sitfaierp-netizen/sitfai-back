package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto;

import java.math.BigDecimal;

/**
 * DTO de respuesta para la consulta de stock de un producto en una Bodega.
 * <p>
 * Devuelto por {@code ConsultarStockUseCase}. Record inmutable (Java 25).
 * <p>
 * El stock retornado es siempre {@code >= 0} — garantizado por la invariante BOD-05
 * del Agregado Bodega.
 *
 * @param bodegaId    UUID de la Bodega consultada.
 * @param productoId  UUID del Producto consultado.
 * @param stock       Cantidad en stock actual (>= 0).
 */
public record StockResponse(
        String bodegaId,
        String productoId,
        BigDecimal stock
) {}
