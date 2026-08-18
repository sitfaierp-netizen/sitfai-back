package com.SITFAI_CORE_ERP_TIENDA.purchasing.application.dto;

import java.util.UUID;

/**
 * DTO de Aplicación: Comando para crear una nueva orden de compra en borrador.
 * Record puro de Java 25 (sin anotaciones web ni Jackson).
 */
public record CrearOrdenBorradorCommand(
        UUID empresaId,
        UUID proveedorId
) {
}
