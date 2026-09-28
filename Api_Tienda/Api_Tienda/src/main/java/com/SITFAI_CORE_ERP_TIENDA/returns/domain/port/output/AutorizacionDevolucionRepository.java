package com.SITFAI_CORE_ERP_TIENDA.returns.domain.port.output;

import com.SITFAI_CORE_ERP_TIENDA.returns.domain.model.rma.AutorizacionDevolucion;
import com.SITFAI_CORE_ERP_TIENDA.returns.domain.model.rma.vo.DevolucionId;
import com.SITFAI_CORE_ERP_TIENDA.returns.domain.model.rma.vo.EmpresaId;

import java.util.Optional;

public interface AutorizacionDevolucionRepository {
    void guardar(EmpresaId empresaId, AutorizacionDevolucion autorizacion);
    Optional<AutorizacionDevolucion> buscarPorId(EmpresaId empresaId, DevolucionId devolucionId);
}
