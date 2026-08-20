package com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.dto;

import java.util.UUID;

public record ActualizarSucursalCommand(
        UUID empresaId,
        UUID sucursalId,
        String codigo,
        String nombre
) {}
