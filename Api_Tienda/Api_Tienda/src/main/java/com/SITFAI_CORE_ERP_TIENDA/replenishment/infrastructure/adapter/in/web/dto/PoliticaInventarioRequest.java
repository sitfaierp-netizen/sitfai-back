package com.SITFAI_CORE_ERP_TIENDA.replenishment.infrastructure.adapter.in.web.dto;

import java.util.UUID;

/**
 * Request DTO para la creación/actualización de una Política de Inventario.
 * Regla MT-01: empresaId viene del JWT, nunca del body del cliente.
 * Regla REGLA-5: DTO desacoplado del dominio.
 */
public record PoliticaInventarioRequest(
        UUID bodegaId,
        UUID productoId,
        int puntoReorden,
        int nivelOptimo,
        boolean activa
) {}
