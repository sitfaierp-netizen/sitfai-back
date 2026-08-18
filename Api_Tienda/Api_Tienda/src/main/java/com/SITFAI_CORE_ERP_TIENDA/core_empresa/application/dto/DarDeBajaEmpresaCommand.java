package com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.dto;

import java.util.UUID;

/**
 * Command DTO: Datos para dar de baja definitiva a una Empresa (EMP-04, EMP-07).
 * Record inmutable de Java 25 puro.
 */
public record DarDeBajaEmpresaCommand(
        UUID empresaId,
        String motivo
) {}
