package com.SITFAI_CORE_ERP_TIENDA.purchasing.infrastructure.adapter.in.web;

public record AgregarLineaWebRequest(
        String productoId,
        String cantidad,
        String costoUnitario
) {
}
