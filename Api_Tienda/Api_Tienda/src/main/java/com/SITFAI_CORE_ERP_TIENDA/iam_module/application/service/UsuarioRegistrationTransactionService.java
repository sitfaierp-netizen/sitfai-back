package com.SITFAI_CORE_ERP_TIENDA.iam_module.application.service;

import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.port.output.UsuarioEventPublisher;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.port.output.UsuarioRegistrationUnitOfWork;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.port.output.UsuarioRepository;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.model.Usuario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
public class UsuarioRegistrationTransactionService implements UsuarioRegistrationUnitOfWork {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioEventPublisher usuarioEventPublisher;

    public UsuarioRegistrationTransactionService(
            UsuarioRepository usuarioRepository,
            UsuarioEventPublisher usuarioEventPublisher
    ) {
        this.usuarioRepository = Objects.requireNonNull(usuarioRepository, "usuarioRepository no puede ser null.");
        this.usuarioEventPublisher = Objects.requireNonNull(usuarioEventPublisher, "usuarioEventPublisher no puede ser null.");
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Usuario guardarPendiente(Usuario usuario) {
        return usuarioRepository.guardar(usuario);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Usuario confirmarIdentidad(Usuario usuario) {
        Usuario guardado = usuarioRepository.guardar(usuario);
        usuarioEventPublisher.publicarTodos(usuario.pullDomainEvents());
        return guardado;
    }
}
