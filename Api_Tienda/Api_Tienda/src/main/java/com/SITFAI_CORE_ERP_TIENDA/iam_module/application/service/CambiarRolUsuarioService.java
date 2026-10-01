package com.SITFAI_CORE_ERP_TIENDA.iam_module.application.service;

import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.dto.CambiarRolUsuarioCommand;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.dto.UsuarioResponse;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.mapper.UsuarioApplicationMapper;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.port.input.CambiarRolUsuarioUseCase;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.port.output.IdentityProvisioningPort;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.port.output.UsuarioEventPublisher;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.port.output.UsuarioRepository;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.exception.UsuarioInvalidoException;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.exception.UsuarioNoEncontradoException;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.model.RolUsuario;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.model.Usuario;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.valueobject.UsuarioId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * Servicio de Aplicación: Orquesta la modificación de rol de un Usuario.
 */
@Service
@Transactional
public class CambiarRolUsuarioService implements CambiarRolUsuarioUseCase {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioEventPublisher usuarioEventPublisher;
    private final IdentityProvisioningPort identityProvisioningPort;

    public CambiarRolUsuarioService(
            UsuarioRepository usuarioRepository,
            UsuarioEventPublisher usuarioEventPublisher,
            IdentityProvisioningPort identityProvisioningPort
    ) {
        this.usuarioRepository = Objects.requireNonNull(usuarioRepository, "usuarioRepository no puede ser null.");
        this.usuarioEventPublisher = Objects.requireNonNull(usuarioEventPublisher, "usuarioEventPublisher no puede ser null.");
        this.identityProvisioningPort = Objects.requireNonNull(identityProvisioningPort, "identityProvisioningPort no puede ser null.");
    }

    @Override
    public UsuarioResponse ejecutar(CambiarRolUsuarioCommand command) {
        if (command == null) {
            throw new UsuarioInvalidoException("El comando de cambio de rol no puede ser nulo.");
        }

        EmpresaId empresaId = EmpresaId.de(command.empresaId());
        UsuarioId usuarioId = UsuarioId.de(command.id());

        Usuario usuario = usuarioRepository.buscarPorId(empresaId, usuarioId)
                .orElseThrow(() -> new UsuarioNoEncontradoException(usuarioId, empresaId));

        RolUsuario nuevoRol;
        try {
            if (command.nuevoRol() == null || command.nuevoRol().isBlank()) {
                throw new UsuarioInvalidoException("El nuevo rol es obligatorio.");
            }
            nuevoRol = RolUsuario.valueOf(command.nuevoRol().trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new UsuarioInvalidoException("El rol '" + command.nuevoRol() + "' no es un rol válido.");
        }

        usuario.cambiarRol(nuevoRol);

        identityProvisioningPort.sincronizarRol(usuario);
        Usuario guardado = usuarioRepository.guardar(usuario);
        usuarioEventPublisher.publicarTodos(usuario.pullDomainEvents());

        return UsuarioApplicationMapper.toResponse(guardado);
    }
}
