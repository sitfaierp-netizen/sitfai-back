package com.SITFAI_CORE_ERP_TIENDA.purchasing.application.dto;

import java.util.UUID;

public record CrearBorradorCommand(
        UUID empresaId,
        UUID proveedorId
) {
    public CrearBorradorCommand {
        if (empresaId == null) {
            throw new IllegalArgumentException("El EmpresaId es obligatorio para orquestación aislada.");
        }
        if (proveedorId == null) {
            throw new IllegalArgumentException("El ProveedorId es obligatorio.");
        }
    }
}
