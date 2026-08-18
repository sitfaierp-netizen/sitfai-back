package com.SITFAI_CORE_ERP_TIENDA.purchasing.application.port.input;

import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.dto.OrdenCompraResponse;

import java.util.UUID;

public interface ConsultarOrdenUseCase {
    OrdenCompraResponse buscarPorId(UUID ordenCompraId, UUID empresaId);
    java.util.List<OrdenCompraResponse> listarPorEmpresa(UUID empresaId);
    java.util.List<OrdenCompraResponse> listarPorProveedor(UUID empresaId, UUID proveedorId);
}
