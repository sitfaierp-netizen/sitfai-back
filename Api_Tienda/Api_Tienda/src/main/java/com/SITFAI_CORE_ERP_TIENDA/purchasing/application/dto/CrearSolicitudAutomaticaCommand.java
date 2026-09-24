package com.SITFAI_CORE_ERP_TIENDA.purchasing.application.dto;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

/**
 * Command DTO inmutable para la creación automática de una Solicitud de Abastecimiento.
 * <p>
 * Generado por el sistema cuando el módulo {@code replenishment} detecta que el stock
 * de un producto perfora el Punto de Reorden. No requiere intervención humana para dispararse.
 * <p>
 * Regla MT-01: El {@code empresaId} proviene del Evento de Dominio (trazado desde el JWT original),
 * nunca de un payload HTTP del cliente.
 * Regla REGLA-5: Record de Java 21 — inmutable y sin dependencias a frameworks.
 */
public record CrearSolicitudAutomaticaCommand(
        UUID empresaId,
        UUID bodegaId,
        UUID productoId,
        BigDecimal cantidadRequerida
) {
    public CrearSolicitudAutomaticaCommand {
        Objects.requireNonNull(empresaId,       "CrearSolicitudAutomaticaCommand: empresaId es obligatorio (MT-01).");
        Objects.requireNonNull(bodegaId,        "CrearSolicitudAutomaticaCommand: bodegaId es obligatorio.");
        Objects.requireNonNull(productoId,      "CrearSolicitudAutomaticaCommand: productoId es obligatorio.");
        Objects.requireNonNull(cantidadRequerida, "CrearSolicitudAutomaticaCommand: cantidadRequerida es obligatoria.");
        if (cantidadRequerida.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "CrearSolicitudAutomaticaCommand: cantidadRequerida debe ser positiva. Recibido: " + cantidadRequerida);
        }
    }
}
