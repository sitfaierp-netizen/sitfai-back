package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.infrastructure.adapter.in.web.dto;

import java.util.UUID;

/**
 * Web Request DTO: Payload HTTP para la creación de un nuevo pedido.
 * <p>
 * Pertenece a la Capa de Infraestructura (REGLA-5).
 */
public record CrearPedidoWebRequest(
        UUID clienteId
) {}
