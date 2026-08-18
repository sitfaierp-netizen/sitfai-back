package com.SITFAI_CORE_ERP_TIENDA.purchasing.application.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record OrdenCompraResponse(
        UUID id,
        UUID empresaId,
        UUID proveedorId,
        String fechaCreacion,
        String estado,
        BigDecimal costoTotal,
        List<LineaResponse> lineas
) {
    public record LineaResponse(
            UUID productoId,
            BigDecimal cantidadSolicitada,
            BigDecimal costoUnitarioPactado,
            BigDecimal subtotal
    ) {}
}
