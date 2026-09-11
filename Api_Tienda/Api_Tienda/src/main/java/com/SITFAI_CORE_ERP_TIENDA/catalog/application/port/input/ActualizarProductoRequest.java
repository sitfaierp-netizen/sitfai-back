package com.SITFAI_CORE_ERP_TIENDA.catalog.application.port.input;

import java.math.BigDecimal;
import java.util.UUID;

public record ActualizarProductoRequest(
        String nombre,
        String descripcion,
        UUID categoriaId,
        String unidadMedida,
        BigDecimal precioCompra,
        BigDecimal precioVenta,
        String impuesto,
        String codigoBarras
) {}
