package com.SITFAI_CORE_ERP_TIENDA.iam_module.application.port.input;

import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.dto.UsuarioResponse;

import java.util.UUID;

public interface ReconciliarIdentidadUseCase {

    UsuarioResponse ejecutar(UUID empresaId, UUID usuarioId);
}
