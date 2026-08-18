package com.SITFAI_CORE_ERP_TIENDA.purchasing.application.dto;

import java.util.UUID;

/**
 * DTO de Aplicación: Comando para remover una línea de detalle de una orden de compra en borrador.
 * Record puro de Java 25.
 */
public record RemoverLineaCommand(
        UUID ordenCompraId,
        UUID empresaId,
        UUID lineaId
) {
}
