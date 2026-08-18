package com.SITFAI_CORE_ERP_TIENDA.iam_module.application.port.input;

import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.dto.UsuarioResponse;

import java.util.List;
import java.util.UUID;

/**
 * Driving Port / Input Port: Caso de Uso para consultar usuarios respetando MT-01.
 */
public interface ConsultarUsuarioUseCase {
    UsuarioResponse obtenerPorId(UUID empresaId, UUID id);
    UsuarioResponse obtenerPorUsername(UUID empresaId, String username);
    List<UsuarioResponse> listarPorEmpresa(UUID empresaId);
}
