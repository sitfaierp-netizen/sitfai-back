package com.SITFAI_CORE_ERP_TIENDA.iam_module.application.service;

import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.dto.RegistrarUsuarioCommand;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.dto.UsuarioResponse;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.mapper.UsuarioApplicationMapper;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.port.input.RegistrarUsuarioUseCase;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.port.output.IdentityProvisioningPort;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.port.output.UsuarioEventPublisher;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.port.output.UsuarioRepository;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.exception.UsuarioInvalidoException;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.model.RolUsuario;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.model.Usuario;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.valueobject.Email;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.valueobject.Username;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.valueobject.UsuarioId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * Servicio de Aplicación: Orquesta el registro de nuevos usuarios en un Tenant.
 */
@Service
@Transactional
public class RegistrarUsuarioService implements RegistrarUsuarioUseCase {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioEventPublisher usuarioEventPublisher;
    private final IdentityProvisioningPort identityProvisioningPort;

    public RegistrarUsuarioService(
            UsuarioRepository usuarioRepository,
            UsuarioEventPublisher usuarioEventPublisher,
            IdentityProvisioningPort identityProvisioningPort
    ) {
        this.usuarioRepository = Objects.requireNonNull(usuarioRepository, "usuarioRepository no puede ser null.");
        this.usuarioEventPublisher = Objects.requireNonNull(usuarioEventPublisher, "usuarioEventPublisher no puede ser null.");
        this.identityProvisioningPort = Objects.requireNonNull(identityProvisioningPort, "identityProvisioningPort no puede ser null.");
    }

    @Override
    public UsuarioResponse ejecutar(RegistrarUsuarioCommand command) {
        if (command == null) {
            throw new UsuarioInvalidoException("El comando de registro de usuario no puede ser nulo.");
        }

        EmpresaId empresaId = EmpresaId.de(command.empresaId());
        Username username = Username.de(command.username());
        Email email = Email.de(command.email());

        if (usuarioRepository.existePorUsername(empresaId, username)) {
            throw new UsuarioInvalidoException("El nombre de usuario '" + username.valor() + "' ya está registrado en esta empresa.");
        }

        if (usuarioRepository.existePorEmail(empresaId, email)) {
            throw new UsuarioInvalidoException("El correo electrónico '" + email.valor() + "' ya está registrado en esta empresa.");
        }

        RolUsuario rol;
        try {
            if (command.rol() == null || command.rol().isBlank()) {
                throw new UsuarioInvalidoException("El rol de usuario es obligatorio.");
            }
            rol = RolUsuario.valueOf(command.rol().trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new UsuarioInvalidoException("El rol '" + command.rol() + "' no es un rol válido en el sistema.");
        }

        UsuarioId usuarioId = command.id() != null ? UsuarioId.de(command.id()) : UsuarioId.generar();

        Usuario usuario = Usuario.registrar(usuarioId, empresaId, username, email, rol);

        // Keycloak se reconcilia antes del commit local. Si la llamada falla, la
        // transacción local revierte y el mismo comando puede reintentarse.
        identityProvisioningPort.provisionar(usuario);
        Usuario guardado = usuarioRepository.guardar(usuario);
        usuarioEventPublisher.publicarTodos(usuario.pullDomainEvents());

        return UsuarioApplicationMapper.toResponse(guardado);
    }
}
