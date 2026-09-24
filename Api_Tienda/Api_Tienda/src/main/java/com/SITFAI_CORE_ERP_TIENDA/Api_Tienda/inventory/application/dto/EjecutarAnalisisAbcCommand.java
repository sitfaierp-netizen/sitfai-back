package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto;

import java.util.Objects;
import java.util.UUID;

/**
 * DTO de entrada: Comando inmutable para solicitar la ejecución del Análisis ABC en una bodega.
 * <p>
 * Regla MT-02: No transporta empresa_id desde el cliente HTTP. El tenant se extrae
 * criptográficamente del token JWT en el Application Service.
 * Regla REGLA-1: DTO puro sin anotaciones de persistencia ni frameworks.
 */
public record EjecutarAnalisisAbcCommand(UUID bodegaId) {

    public EjecutarAnalisisAbcCommand {
        Objects.requireNonNull(bodegaId, "EjecutarAnalisisAbcCommand: bodegaId no puede ser nulo.");
    }
}
