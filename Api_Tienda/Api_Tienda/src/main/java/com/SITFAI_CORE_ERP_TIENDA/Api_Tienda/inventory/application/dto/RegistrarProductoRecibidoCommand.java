package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto;

import java.util.UUID;

public record RegistrarProductoRecibidoCommand(
    UUID recepcionId,
    UUID productoId,
    int cantidadRecepcion
) {
    public RegistrarProductoRecibidoCommand {
        if (recepcionId == null) throw new IllegalArgumentException("recepcionId es requerido");
        if (productoId == null) throw new IllegalArgumentException("productoId es requerido");
        if (cantidadRecepcion < 0) throw new IllegalArgumentException("cantidadRecepcion debe ser mayor o igual a 0");
    }
}
