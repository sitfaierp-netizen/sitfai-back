package com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.event;

import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.valueobject.UsuarioId;

import java.time.Instant;
import java.util.UUID;

/**
 * Evento emitido cuando un Usuario inactivo es reactivado.
 */
public record UsuarioReactivadoEvent(
        UUID eventoId,
        UsuarioId usuarioId,
        EmpresaId empresaId,
        Instant ocurridoEn
) implements DomainEvent {

    public UsuarioReactivadoEvent(
            UsuarioId usuarioId,
            EmpresaId empresaId,
            Instant ocurridoEn
    ) {
        this(UUID.randomUUID(), usuarioId, empresaId, ocurridoEn);
    }

    @Override
    public String tipoEvento() {
        return "USUARIO_REACTIVADO";
    }
}
