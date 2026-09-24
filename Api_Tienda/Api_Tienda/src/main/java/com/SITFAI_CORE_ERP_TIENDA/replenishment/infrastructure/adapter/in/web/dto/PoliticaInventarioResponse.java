package com.SITFAI_CORE_ERP_TIENDA.replenishment.infrastructure.adapter.in.web.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO para la API REST de Políticas de Inventario.
 * Regla REGLA-5: DTO desacoplado del dominio — el controlador nunca devuelve objetos de dominio.
 */
public record PoliticaInventarioResponse(
        UUID id,
        UUID empresaId,
        UUID bodegaId,
        UUID productoId,
        int puntoReorden,
        int nivelOptimo,
        boolean activa,
        Instant creadoEn,
        Instant actualizadoEn
) {}
