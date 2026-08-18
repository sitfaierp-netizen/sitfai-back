package com.SITFAI_CORE_ERP_TIENDA.core_empresa.infrastructure.adapter.in.web.dto;

import java.util.UUID;

/**
 * Web Request DTO para el registro de una nueva Empresa (Tenant Raíz).
 */
public record RegistrarEmpresaRequest(
        UUID id,
        String ruc,
        String nombre,
        String codigoSucursalPrincipal,
        String nombreSucursalPrincipal
) {}
