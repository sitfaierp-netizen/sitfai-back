package com.SITFAI_CORE_ERP_TIENDA.pos.domain.event;

import com.SITFAI_CORE_ERP_TIENDA.pos.domain.valueobject.CajaId;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.valueobject.Dinero;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.valueobject.TurnoId;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.valueobject.UsuarioId;

import java.time.Instant;
import java.util.UUID;

public record TurnoCerradoEvent(
        UUID eventoId,
        TurnoId turnoId,
        EmpresaId empresaId,
        CajaId cajaId,
        UsuarioId usuarioId,
        Dinero consolidadoFinal,
        Instant ocurridoEn
) implements DomainEvent {
    public static TurnoCerradoEvent of(TurnoId turnoId, EmpresaId empresaId, CajaId cajaId, UsuarioId usuarioId, Dinero consolidadoFinal) {
        return new TurnoCerradoEvent(
                UUID.randomUUID(),
                turnoId,
                empresaId,
                cajaId,
                usuarioId,
                consolidadoFinal,
                Instant.now()
        );
    }
}
