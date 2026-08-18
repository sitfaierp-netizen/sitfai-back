package com.SITFAI_CORE_ERP_TIENDA.billing.infrastructure.adapter.in.web.dto;

/**
 * Web Request DTO: Parámetros HTTP para la anulación de una Factura.
 */
public record AnularFacturaWebRequest(
        String motivo
) {}
