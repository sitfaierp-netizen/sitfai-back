package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.port.input;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.BodegaResponse;

import java.util.List;

public interface ObtenerBodegasPorSucursalUseCase {
    List<BodegaResponse> ejecutar(String empresaId, String sucursalId);
}
