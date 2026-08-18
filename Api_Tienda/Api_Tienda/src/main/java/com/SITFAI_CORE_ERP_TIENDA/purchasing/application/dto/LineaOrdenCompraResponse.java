package com.SITFAI_CORE_ERP_TIENDA.purchasing.application.dto;

import java.util.UUID;

public record LineaOrdenCompraResponse(
        String id,
        UUID productoId,
        String cantidadSolicitada,
        String costoUnitarioEsperado,
        String subtotal
) {
}
