package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.in.web.dto;

import java.math.BigDecimal;

/**
 * Web Request DTO: Payload HTTP para registrar un movimiento de inventario.
 * <p>
 * Pertenece exclusivamente a la Capa de Infraestructura (REGLA-5).
 * Valida los parámetros del contrato HTTP antes de delegar a la Capa de Aplicación.
 */
public record RegistrarMovimientoWebRequest(
        String productoId,
        BigDecimal cantidad,
        String tipo,
        String docFuenteTipo,
        String docFuenteNumero
) {}
