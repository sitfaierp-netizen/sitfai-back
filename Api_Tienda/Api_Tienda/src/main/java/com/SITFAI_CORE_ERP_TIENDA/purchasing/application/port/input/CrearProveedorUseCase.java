package com.SITFAI_CORE_ERP_TIENDA.purchasing.application.port.input;

import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.dto.CrearProveedorRequest;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.dto.ProveedorResponse;

import java.util.List;
import java.util.UUID;

public interface CrearProveedorUseCase {
    void crearProveedor(UUID empresaId, CrearProveedorRequest request);
    List<ProveedorResponse> listarProveedores(UUID empresaId);
}
