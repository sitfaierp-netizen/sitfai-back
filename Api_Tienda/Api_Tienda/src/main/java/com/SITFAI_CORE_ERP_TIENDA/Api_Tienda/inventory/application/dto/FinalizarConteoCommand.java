package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto;

import java.util.Objects;
import java.util.UUID;

/**
 * DTO inmutable de entrada: Comando para solicitar la finalización y evaluación analítica de un conteo cíclico.
 * <p>
 * Regla MT-02: No transporta empresa_id desde el cliente HTTP.
 * Regla REGLA-1: DTO puro sin dependencias de frameworks externos.
 */
public record FinalizarConteoCommand(UUID conteoId) {

    public FinalizarConteoCommand {
        Objects.requireNonNull(conteoId, "FinalizarConteoCommand: conteoId es obligatorio.");
    }
}
