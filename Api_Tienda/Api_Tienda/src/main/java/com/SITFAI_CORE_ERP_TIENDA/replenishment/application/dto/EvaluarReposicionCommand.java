package com.SITFAI_CORE_ERP_TIENDA.replenishment.application.dto;

import java.util.UUID;

/**
 * Command DTO de entrada para el caso de uso de evaluación de reposición.
 * Regla MT-01: El empresaId se inyecta desde el contexto de seguridad, no del cliente.
 * Regla REGLA-5: DTO desacoplado del dominio y de la web layer.
 */
public record EvaluarReposicionCommand(
        UUID bodegaId,
        UUID productoId,
        int stockDisponible
) {
    public EvaluarReposicionCommand {
        if (bodegaId == null) throw new IllegalArgumentException("bodegaId es obligatorio");
        if (productoId == null) throw new IllegalArgumentException("productoId es obligatorio");
        if (stockDisponible < 0) throw new IllegalArgumentException("stockDisponible no puede ser negativo");
    }
}
