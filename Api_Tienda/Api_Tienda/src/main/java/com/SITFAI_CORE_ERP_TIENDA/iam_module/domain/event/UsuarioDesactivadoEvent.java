package com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.event;

import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.valueobject.UsuarioId;

import java.time.Instant;
import java.util.UUID;

/**
 * Evento emitido cuando un Usuario es desactivado / inhabilitado.
 */
public record UsuarioDesactivadoEvent(
        UUID eventoId,
        UsuarioId usuarioId,
        EmpresaId empresaId,
        String motivo,
        Instant ocurridoEn
) implements DomainEvent {

    public UsuarioDesactivadoEvent(
            UsuarioId usuarioId,
            EmpresaId empresaId,
            String motivo,
            Instant ocurridoEn
    ) {
        this(UUID.randomUUID(), usuarioId, empresaId, motivo, ocurridoEn);
    }

    @Override
    public String tipoEvento() {
        return "USUARIO_DESACTIVADO";
    }
}
