package com.SITFAI_CORE_ERP_TIENDA.purchasing.infrastructure.adapter.in.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * DTO Web de Entrada (Driving Adapter Request):
 * <p>
 * Regla MT-02: El {@code empresa_id} jamás se acepta en el payload; se resuelve por seguridad.
 * Regla 5: DTO de entrada HTTP aislado del dominio y del command de aplicación.
 */
public record EmitirOrdenCompraWebRequest(
        @NotNull(message = "El proveedorId es obligatorio")
        UUID proveedorId,

        @NotEmpty(message = "Debe incluir al menos una línea de orden de compra")
        @Valid
        List<LineaOrdenCompraWebRequest> lineas
) {
    public record LineaOrdenCompraWebRequest(
            @NotNull(message = "El productoId es obligatorio")
            UUID productoId,

            @Positive(message = "La cantidad solicitada debe ser mayor a cero")
            int cantidadSolicitada,

            @NotNull(message = "El costo unitario esperado es obligatorio")
            BigDecimal costoUnitarioEsperado
    ) {}
}
