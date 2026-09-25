package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Web Request DTO: Carga útil para el registro de conteo físico de un producto en estantería.
 * <p>
 * Regla MT-02: No incluye empresaId; el aislamiento se extrae en el caso de uso.
 */
public record RegistrarConteoFisicoWebRequest(
        @NotNull(message = "El productoId es obligatorio")
        UUID productoId,

        @NotNull(message = "La cantidadFisica es obligatoria")
        @Min(value = 0, message = "La cantidad física no puede ser negativa")
        Integer cantidadFisica
) {
}
