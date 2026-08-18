package com.SITFAI_CORE_ERP_TIENDA.core_empresa.infrastructure.adapter.in.web.dto;

/**
 * Web Request DTO para transiciones de estado de una Empresa (EMP-04, EMP-06, EMP-07).
 */
public record CambiarEstadoEmpresaRequest(
        String accion,
        String motivo
) {}
