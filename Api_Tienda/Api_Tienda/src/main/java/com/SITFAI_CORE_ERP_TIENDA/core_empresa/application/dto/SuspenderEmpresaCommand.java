package com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.dto;

import java.util.UUID;

/**
 * Command DTO: Datos para suspender temporalmente una Empresa (EMP-06).
 * Record inmutable de Java 25 puro.
 */
public record SuspenderEmpresaCommand(
        UUID empresaId,
        String motivo
) {}
