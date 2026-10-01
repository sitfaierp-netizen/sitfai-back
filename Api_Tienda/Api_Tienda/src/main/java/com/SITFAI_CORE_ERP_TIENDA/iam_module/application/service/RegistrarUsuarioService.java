package com.SITFAI_CORE_ERP_TIENDA.iam_module.application.service;

import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.dto.RegistrarUsuarioCommand;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.dto.UsuarioResponse;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.mapper.UsuarioApplicationMapper;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.port.input.RegistrarUsuarioUseCase;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.port.output.IdentityProvisioningPort;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.port.output.UsuarioRegistrationUnitOfWork;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.port.output.UsuarioRepository;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.exception.UsuarioInvalidoException;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.model.RolUsuario;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.model.Usuario;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.valueobject.Email;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.valueobject.Username;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.valueobject.UsuarioId;
import org.springframework.stereotype.Service;
import java.util.Objects;
import java.util.Optional;

/**
 * Servicio de Aplicación: Orquesta el registro de nuevos usuarios en un Tenant.
 */
@Service
public class RegistrarUsuarioService implements RegistrarUsuarioUseCase {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioRegistrationUnitOfWork registrationUnitOfWork;
    private final IdentityProvisioningPort identityProvisioningPort;

    public RegistrarUsuarioService(
            UsuarioRepository usuarioRepository,
            UsuarioRegistrationUnitOfWork registrationUnitOfWork,
            IdentityProvisioningPort identityProvisioningPort
    ) {
        this.usuarioRepository = Objects.requireNonNull(usuarioRepository, "usuarioRepository no puede ser null.");
        this.registrationUnitOfWork = Objects.requireNonNull(registrationUnitOfWork, "registrationUnitOfWork no puede ser null.");
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

        RolUsuario rol;
        try {
            if (command.rol() == null || command.rol().isBlank()) {
                throw new UsuarioInvalidoException("El rol de usuario es obligatorio.");
            }
            rol = RolUsuario.valueOf(command.rol().trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new UsuarioInvalidoException("El rol '" + command.rol() + "' no es un rol válido en el sistema.");
        }

        Optional<Usuario> porUsername = usuarioRepository.buscarPorUsername(empresaId, username);
        Optional<Usuario> porEmail = usuarioRepository.buscarPorEmail(empresaId, email);
        Usuario usuario = resolverUsuarioRecuperable(command, empresaId, username, email, rol, porUsername, porEmail);

        if (usuario.estaActivo()) {
            identityProvisioningPort.reconciliar(usuario);
            return UsuarioApplicationMapper.toResponse(usuario);
        }

        // La fila pendiente ya está confirmada antes de cualquier efecto externo.
        // Keycloak se prepara deshabilitado y todavía no envía onboarding.
        identityProvisioningPort.provisionar(usuario);
        usuario.confirmarIdentidad();
        Usuario guardado = registrationUnitOfWork.confirmarIdentidad(usuario);

        // Sólo una entidad local confirmada puede habilitarse y recibir onboarding.
        identityProvisioningPort.completarOnboarding(guardado);

        return UsuarioApplicationMapper.toResponse(guardado);
    }

    private Usuario resolverUsuarioRecuperable(
            RegistrarUsuarioCommand command,
            EmpresaId empresaId,
            Username username,
            Email email,
            RolUsuario rol,
            Optional<Usuario> porUsername,
            Optional<Usuario> porEmail
    ) {
        if (porUsername.isPresent() || porEmail.isPresent()) {
            Usuario existente = porUsername.orElseGet(porEmail::orElseThrow);
            if (porUsername.isEmpty() || porEmail.isEmpty()
                    || !porUsername.get().getId().equals(porEmail.get().getId())) {
                throw new UsuarioInvalidoException("El username o correo ya pertenece a otro usuario de la empresa.");
            }
            validarMismoPayload(command, existente, username, email, rol);
            if (!existente.estaPendienteIdentidad() && !existente.estaActivo()) {
                throw new UsuarioInvalidoException("El usuario existente no admite reintento de provisioning.");
            }
            return existente;
        }

        UsuarioId usuarioId = command.id() != null ? UsuarioId.de(command.id()) : UsuarioId.generar();
        Usuario nuevo = Usuario.registrar(usuarioId, empresaId, username, email, rol);
        return registrationUnitOfWork.guardarPendiente(nuevo);
    }

    private void validarMismoPayload(
            RegistrarUsuarioCommand command,
            Usuario existente,
            Username username,
            Email email,
            RolUsuario rol
    ) {
        if (!existente.getUsername().equals(username)
                || !existente.getEmail().equals(email)
                || existente.getRol() != rol
                || command.id() != null && !existente.getId().valor().equals(command.id())) {
            throw new UsuarioInvalidoException("El reintento no coincide con el usuario local existente.");
        }
    }
}
