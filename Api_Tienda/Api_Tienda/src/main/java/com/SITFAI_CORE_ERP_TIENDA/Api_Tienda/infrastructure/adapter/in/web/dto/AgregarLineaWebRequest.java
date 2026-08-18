package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.infrastructure.adapter.in.web.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Web Request DTO: Payload HTTP para agregar una línea a un pedido existente.
 * <p>
 * Pertenece a la Capa de Infraestructura (REGLA-5).
 */
public record AgregarLineaWebRequest(
        UUID productoId,
        int cantidad,
        BigDecimal precioUnitario,
        String moneda
) {}
