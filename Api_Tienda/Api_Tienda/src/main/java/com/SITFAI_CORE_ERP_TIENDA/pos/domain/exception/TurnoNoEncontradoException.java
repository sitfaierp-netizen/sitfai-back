package com.SITFAI_CORE_ERP_TIENDA.pos.domain.exception;

import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.turno.vo.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.turno.vo.TurnoId;

public class TurnoNoEncontradoException extends RuntimeException {
    public TurnoNoEncontradoException(TurnoId turnoId, EmpresaId empresaId) {
        super("Turno " + turnoId.value() + " no encontrado para la empresa " + empresaId.value());
    }
}
