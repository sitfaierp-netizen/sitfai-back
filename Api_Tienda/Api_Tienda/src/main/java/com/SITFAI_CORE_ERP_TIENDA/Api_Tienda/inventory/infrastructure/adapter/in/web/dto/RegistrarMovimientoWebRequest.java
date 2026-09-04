package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

/**
 * Web Request DTO: Payload HTTP para registrar un movimiento de inventario.
 * <p>
 * Pertenece exclusivamente a la Capa de Infraestructura (REGLA-5).
 * Valida los parámetros del contrato HTTP antes de delegar a la Capa de Aplicación.
 */
public record RegistrarMovimientoWebRequest(
        @NotBlank(message = "productoId es obligatorio") String productoId,
        @NotNull(message = "cantidad es obligatoria") @Positive(message = "cantidad debe ser mayor a cero") BigDecimal cantidad,
        @NotBlank(message = "tipo es obligatorio") String tipo, // Ej: ENTRADA, SALIDA
        @NotBlank(message = "docFuenteTipo es obligatorio") String docFuenteTipo,
        @NotBlank(message = "docFuenteNumero es obligatorio") String docFuenteNumero
) {}
