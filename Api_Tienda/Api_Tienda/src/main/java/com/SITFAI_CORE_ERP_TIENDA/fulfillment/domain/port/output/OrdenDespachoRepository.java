package com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.port.output;

import com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.model.despacho.OrdenDespacho;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.model.despacho.vo.DespachoId;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.model.despacho.vo.EmpresaId;

import java.util.Optional;

public interface OrdenDespachoRepository {
    void guardar(EmpresaId empresaId, OrdenDespacho ordenDespacho);
    Optional<OrdenDespacho> buscarPorId(EmpresaId empresaId, DespachoId despachoId);
}
