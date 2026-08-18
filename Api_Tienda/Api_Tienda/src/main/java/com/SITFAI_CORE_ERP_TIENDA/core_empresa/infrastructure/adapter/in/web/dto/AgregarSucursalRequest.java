package com.SITFAI_CORE_ERP_TIENDA.core_empresa.infrastructure.adapter.in.web.dto;

/**
 * Web Request DTO para agregar una sucursal a una Empresa.
 */
public record AgregarSucursalRequest(
        String codigo,
        String nombre
) {}
