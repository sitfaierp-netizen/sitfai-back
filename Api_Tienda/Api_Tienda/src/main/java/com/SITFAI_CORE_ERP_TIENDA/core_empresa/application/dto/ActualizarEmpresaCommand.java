package com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.dto;

import java.util.UUID;

public record ActualizarEmpresaCommand(
        UUID empresaId,
        String ruc,
        String razonSocial
) {}
