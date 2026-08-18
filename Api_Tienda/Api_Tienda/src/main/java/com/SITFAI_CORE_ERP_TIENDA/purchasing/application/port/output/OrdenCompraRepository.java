package com.SITFAI_CORE_ERP_TIENDA.purchasing.application.port.output;

import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.OrdenCompra;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.valueobject.OrdenCompraId;

import java.util.Optional;

public interface OrdenCompraRepository {
    void guardar(OrdenCompra ordenCompra);
    Optional<OrdenCompra> buscarPorId(OrdenCompraId id, EmpresaId empresaId);
}
