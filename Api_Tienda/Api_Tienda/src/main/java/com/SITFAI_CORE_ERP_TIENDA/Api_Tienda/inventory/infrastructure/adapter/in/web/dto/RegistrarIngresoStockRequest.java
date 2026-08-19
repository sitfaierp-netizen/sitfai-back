package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Web DTO para la petición de ingreso de stock.
 * Cumple con REGLA-5: No usar DTOs de Aplicación en la capa Web.
 * Cumple con MT-01: No incluye empresaId.
 */
public record RegistrarIngresoStockRequest(
        @NotNull(message = "El ID de producto es obligatorio")
        UUID productoId,
        
        @NotNull(message = "La cantidad es obligatoria")
        @Positive(message = "La cantidad debe ser mayor a cero")
        BigDecimal cantidad,
        
        String loteId, // Opcional
        
        Instant fechaCaducidad, // Opcional
        
        @NotBlank(message = "El tipo de documento fuente es obligatorio")
        String docFuenteTipo,
        
        @NotBlank(message = "El número de documento fuente es obligatorio")
        String docFuenteNumero
) {
}
