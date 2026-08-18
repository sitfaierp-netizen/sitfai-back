package com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.event;

import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.valueobject.UsuarioId;

import java.time.Instant;
import java.util.UUID;

/**
 * Evento emitido tras el registro de un nuevo Usuario.
 * Utilizado posteriormente para la sincronización con Keycloak.
 */
public record UsuarioRegistradoEvent(
        UUID eventoId,
        UsuarioId usuarioId,
        EmpresaId empresaId,
        String username,
        String email,
        String rol,
        Instant ocurridoEn
) implements DomainEvent {

    public UsuarioRegistradoEvent(
            UsuarioId usuarioId,
            EmpresaId empresaId,
            String username,
            String email,
            String rol,
            Instant ocurridoEn
    ) {
        this(UUID.randomUUID(), usuarioId, empresaId, username, email, rol, ocurridoEn);
    }

    @Override
    public String tipoEvento() {
        return "USUARIO_REGISTRADO";
    }
}
