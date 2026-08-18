package com.SITFAI_CORE_ERP_TIENDA.billing.infrastructure.adapter.in.web.dto;

import java.util.List;
import java.util.UUID;

/**
 * Web Request DTO: Parámetros HTTP para la emisión manual de una Factura.
 */
public record EmitirFacturaWebRequest(
        UUID pedidoOrigenId,
        UUID clienteId,
        String rucCliente,
        String numeroComprobante,
        List<LineaFacturaWebRequest> lineas
) {}
