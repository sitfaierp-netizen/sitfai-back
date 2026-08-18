package com.SITFAI_CORE_ERP_TIENDA.pos.application.port.output;

import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.TurnoCaja;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.valueobject.TurnoId;

import java.util.Optional;

public interface TurnoCajaRepository {
    TurnoCaja guardar(TurnoCaja turno);
    Optional<TurnoCaja> buscarPorId(TurnoId id, EmpresaId empresaId);
}
