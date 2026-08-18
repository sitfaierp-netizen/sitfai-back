package com.SITFAI_CORE_ERP_TIENDA.billing.application.dto;

import java.util.UUID;

/**
 * Application Command: Parámetros inmutables para la anulación de una Factura.
 * <p>
 * Record puro de Java 25.
 */
public record AnularFacturaCommand(
        UUID empresaId,
        UUID facturaId,
        String motivo
) {}
