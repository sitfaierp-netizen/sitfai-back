package com.SITFAI_CORE_ERP_TIENDA.fulfillment.application.port.output;

import com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.model.OrdenDespacho;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.valueobject.DespachoId;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.valueobject.EmpresaId;

import java.util.Optional;

public interface OrdenDespachoRepository {
    OrdenDespacho guardar(OrdenDespacho orden);
    Optional<OrdenDespacho> buscarPorId(DespachoId id, EmpresaId empresaId);
}
