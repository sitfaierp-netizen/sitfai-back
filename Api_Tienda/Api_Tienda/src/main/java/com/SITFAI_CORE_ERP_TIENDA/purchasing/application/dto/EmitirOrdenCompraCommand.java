package com.SITFAI_CORE_ERP_TIENDA.purchasing.application.dto;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Comando inmutable (DTO puro de Aplicación) para solicitar la emisión de una Orden de Compra.
 * <p>
 * Regla 1 (Clean Architecture): Cero anotaciones de framework (ni Spring, ni JPA, ni Jackson).
 * Regla MT-01: El aislamiento se coordina a través de TenantProviderPort (o valor inyectado).
 */
public record EmitirOrdenCompraCommand(
        UUID proveedorId,
        List<LineaOrdenCompraCommand> lineas,
        UUID empresaId
) {

    public EmitirOrdenCompraCommand {
        Objects.requireNonNull(proveedorId, "proveedorId es obligatorio");
        lineas = lineas != null ? Collections.unmodifiableList(lineas) : Collections.emptyList();
    }

    public EmitirOrdenCompraCommand(UUID proveedorId, List<LineaOrdenCompraCommand> lineas) {
        this(proveedorId, lineas, null);
    }

    public record LineaOrdenCompraCommand(
            UUID productoId,
            int cantidadSolicitada,
            BigDecimal costoUnitarioEsperado
    ) {
        public LineaOrdenCompraCommand {
            Objects.requireNonNull(productoId, "productoId es obligatorio");
            if (cantidadSolicitada <= 0) {
                throw new IllegalArgumentException("cantidadSolicitada debe ser estrictamente mayor a cero");
            }
            Objects.requireNonNull(costoUnitarioEsperado, "costoUnitarioEsperado es obligatorio");
            if (costoUnitarioEsperado.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("costoUnitarioEsperado no puede ser negativo");
            }
        }
    }
}
