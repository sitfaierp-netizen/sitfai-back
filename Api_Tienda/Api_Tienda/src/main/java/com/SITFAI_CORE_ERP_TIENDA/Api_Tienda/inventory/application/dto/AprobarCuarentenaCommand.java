package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto;

import java.math.BigDecimal;

/**
 * Command DTO para aprobar o rechazar mercadería en cuarentena.
 */
public record AprobarCuarentenaCommand(
        String empresaId,
        String sucursalId,
        String productoId,
        BigDecimal cantidad,
        boolean aprobado
) {
    public AprobarCuarentenaCommand {
        if (empresaId == null || empresaId.isBlank()) {
            throw new IllegalArgumentException("EmpresaId es obligatorio");
        }
        if (sucursalId == null || sucursalId.isBlank()) {
            throw new IllegalArgumentException("SucursalId es obligatorio");
        }
        if (productoId == null || productoId.isBlank()) {
            throw new IllegalArgumentException("ProductoId es obligatorio");
        }
        if (cantidad == null || cantidad.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
        }
    }
}
