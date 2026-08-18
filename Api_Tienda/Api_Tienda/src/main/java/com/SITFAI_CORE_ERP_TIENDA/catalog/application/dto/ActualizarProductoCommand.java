package com.SITFAI_CORE_ERP_TIENDA.catalog.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Command para actualizar datos mutables de un Producto.
 * PROHIBIDO incluir empresaId — se extrae del JWT vía TenantProviderPort (MT-01, MT-06).
 * El SKU es inmutable una vez creado.
 */
public record ActualizarProductoCommand(
        UUID         productoId,
        String       nombre,
        String       descripcion,
        UUID         categoriaId,
        String       unidadMedida,
        BigDecimal   precioCompra,
        BigDecimal   precioVenta,
        String       impuesto,
        String       codigoBarras   // nullable
) {}
