package com.SITFAI_CORE_ERP_TIENDA.catalog.application.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * DTO de respuesta para Producto.
 * El Dominio nunca cruza la frontera HTTP (Regla 5).
 */
public record ProductoResponse(
        UUID       productoId,
        UUID       empresaId,
        String     sku,
        String     nombre,
        String     descripcion,
        UUID       categoriaId,
        String     unidadMedida,
        BigDecimal precioCompra,
        BigDecimal precioVenta,
        String     impuesto,
        String     codigoBarras,
        String     estado,
        Instant    creadoEn,
        Instant    actualizadoEn
) {}
