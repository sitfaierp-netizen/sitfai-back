package com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.dto;

import java.util.UUID;

/**
 * Command DTO: Datos necesarios para agregar una sucursal a una Empresa.
 * Record inmutable de Java 25 puro.
 */
public record AgregarSucursalCommand(
        UUID empresaId,
        String codigo,
        String nombre
) {}
