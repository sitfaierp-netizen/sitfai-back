package com.SITFAI_CORE_ERP_TIENDA.iam_module.application.service;

import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.dto.DesactivarUsuarioCommand;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.dto.ReactivarUsuarioCommand;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.dto.UsuarioResponse;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.mapper.UsuarioApplicationMapper;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.port.input.DesactivarUsuarioUseCase;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.port.input.ReactivarUsuarioUseCase;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.port.output.UsuarioEventPublisher;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.port.output.UsuarioRepository;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.exception.UsuarioInvalidoException;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.exception.UsuarioNoEncontradoException;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.model.Usuario;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.valueobject.UsuarioId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * Servicio de Aplicación: Orquesta las transiciones de estado (desactivación y reactivación) de un Usuario.
 */
@Service
@Transactional
public class GestionarEstadoUsuarioService implements DesactivarUsuarioUseCase, ReactivarUsuarioUseCase {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioEventPublisher usuarioEventPublisher;

    public GestionarEstadoUsuarioService(
            UsuarioRepository usuarioRepository,
            UsuarioEventPublisher usuarioEventPublisher
    ) {
        this.usuarioRepository = Objects.requireNonNull(usuarioRepository, "usuarioRepository no puede ser null.");
        this.usuarioEventPublisher = Objects.requireNonNull(usuarioEventPublisher, "usuarioEventPublisher no puede ser null.");
    }

    @Override
    public UsuarioResponse ejecutar(DesactivarUsuarioCommand command) {
        if (command == null) {
            throw new UsuarioInvalidoException("El comando de desactivación no puede ser nulo.");
        }

        EmpresaId empresaId = EmpresaId.de(command.empresaId());
        UsuarioId usuarioId = UsuarioId.de(command.id());

        Usuario usuario = usuarioRepository.buscarPorId(empresaId, usuarioId)
                .orElseThrow(() -> new UsuarioNoEncontradoException(usuarioId, empresaId));

        usuario.desactivar(command.motivo());

        Usuario guardado = usuarioRepository.guardar(usuario);
        usuarioEventPublisher.publicarTodos(usuario.pullDomainEvents());

        return UsuarioApplicationMapper.toResponse(guardado);
    }

    @Override
    public UsuarioResponse ejecutar(ReactivarUsuarioCommand command) {
        if (command == null) {
            throw new UsuarioInvalidoException("El comando de reactivación no puede ser nulo.");
        }

        EmpresaId empresaId = EmpresaId.de(command.empresaId());
        UsuarioId usuarioId = UsuarioId.de(command.id());

        Usuario usuario = usuarioRepository.buscarPorId(empresaId, usuarioId)
                .orElseThrow(() -> new UsuarioNoEncontradoException(usuarioId, empresaId));

        usuario.reactivar();

        Usuario guardado = usuarioRepository.guardar(usuario);
        usuarioEventPublisher.publicarTodos(usuario.pullDomainEvents());

        return UsuarioApplicationMapper.toResponse(guardado);
    }
}
