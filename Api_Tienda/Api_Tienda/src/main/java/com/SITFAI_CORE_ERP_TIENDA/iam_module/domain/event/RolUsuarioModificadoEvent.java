package com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.event;

import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.valueobject.UsuarioId;

import java.time.Instant;
import java.util.UUID;

/**
 * Evento emitido tras la modificación del rol de un Usuario.
 */
public record RolUsuarioModificadoEvent(
        UUID eventoId,
        UsuarioId usuarioId,
        EmpresaId empresaId,
        String rolAnterior,
        String nuevoRol,
        Instant ocurridoEn
) implements DomainEvent {

    public RolUsuarioModificadoEvent(
            UsuarioId usuarioId,
            EmpresaId empresaId,
            String rolAnterior,
            String nuevoRol,
            Instant ocurridoEn
    ) {
        this(UUID.randomUUID(), usuarioId, empresaId, rolAnterior, nuevoRol, ocurridoEn);
    }

    @Override
    public String tipoEvento() {
        return "ROL_USUARIO_MODIFICADO";
    }
}
