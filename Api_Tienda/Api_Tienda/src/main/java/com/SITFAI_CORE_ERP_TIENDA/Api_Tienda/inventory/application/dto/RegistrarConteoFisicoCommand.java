package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto;

import java.util.Objects;
import java.util.UUID;

/**
 * DTO inmutable de entrada: Comando para registrar la cantidad física constatada de un producto en un conteo cíclico.
 * <p>
 * Regla MT-02: No transporta empresa_id desde el cliente HTTP; el tenant se extrae
 * criptográficamente en el Application Service.
 * Regla REGLA-1: DTO puro sin dependencias de frameworks externos.
 */
public record RegistrarConteoFisicoCommand(
        UUID conteoId,
        UUID productoId,
        int cantidadFisica
) {

    public RegistrarConteoFisicoCommand {
        Objects.requireNonNull(conteoId, "RegistrarConteoFisicoCommand: conteoId es obligatorio.");
        Objects.requireNonNull(productoId, "RegistrarConteoFisicoCommand: productoId es obligatorio.");
        if (cantidadFisica < 0) {
            throw new IllegalArgumentException("RegistrarConteoFisicoCommand: cantidadFisica no puede ser negativa. Recibido: " + cantidadFisica);
        }
    }
}
