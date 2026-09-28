package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.port.output;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.recepcion.RecepcionMercancia;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.recepcion.vo.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.recepcion.vo.RecepcionId;

import java.util.Optional;

public interface RecepcionMercanciaRepository {
    void guardar(RecepcionMercancia recepcion, EmpresaId empresaId);
    Optional<RecepcionMercancia> buscarPorId(RecepcionId id, EmpresaId empresaId);
}
