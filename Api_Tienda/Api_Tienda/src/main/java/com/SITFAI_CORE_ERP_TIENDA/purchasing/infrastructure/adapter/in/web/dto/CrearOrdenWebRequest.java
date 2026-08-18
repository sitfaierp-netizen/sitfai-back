package com.SITFAI_CORE_ERP_TIENDA.purchasing.infrastructure.adapter.in.web.dto;

import java.util.UUID;

/**
 * DTO Web: Petición para crear una orden de compra en borrador.
 */
public record CrearOrdenWebRequest(
        UUID proveedorId,
        UUID empresaId
) {
}
