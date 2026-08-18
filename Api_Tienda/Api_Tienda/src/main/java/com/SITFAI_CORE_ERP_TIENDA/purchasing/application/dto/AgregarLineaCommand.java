package com.SITFAI_CORE_ERP_TIENDA.purchasing.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record AgregarLineaCommand(
        UUID ordenCompraId,
        UUID empresaId,
        UUID productoId,
        BigDecimal cantidad,
        BigDecimal costoUnitario
) {
    public AgregarLineaCommand {
        if (ordenCompraId == null) throw new IllegalArgumentException("El ID de la orden es obligatorio.");
        if (empresaId == null) throw new IllegalArgumentException("El EmpresaId es obligatorio.");
        if (productoId == null) throw new IllegalArgumentException("El ProductoId es obligatorio.");
        if (cantidad == null || cantidad.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero.");
        }
        if (costoUnitario == null || costoUnitario.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El costo unitario no puede ser negativo.");
        }
    }
}
