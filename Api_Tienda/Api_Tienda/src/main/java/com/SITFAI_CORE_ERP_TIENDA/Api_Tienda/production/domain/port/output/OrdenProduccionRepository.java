package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.domain.port.output;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.domain.model.orden.OrdenProduccion;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.domain.model.orden.vo.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.domain.model.orden.vo.OrdenProduccionId;

import java.util.Optional;

public interface OrdenProduccionRepository {
    void guardar(OrdenProduccion ordenProduccion, EmpresaId empresaId);
    Optional<OrdenProduccion> buscarPorId(OrdenProduccionId id, EmpresaId empresaId);
}
