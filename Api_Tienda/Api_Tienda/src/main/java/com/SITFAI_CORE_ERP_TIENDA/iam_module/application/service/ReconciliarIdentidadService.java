package com.SITFAI_CORE_ERP_TIENDA.iam_module.application.service;

import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.dto.UsuarioResponse;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.mapper.UsuarioApplicationMapper;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.port.input.ReconciliarIdentidadUseCase;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.port.output.IdentityProvisioningPort;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.port.output.UsuarioRepository;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.exception.UsuarioNoEncontradoException;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.model.Usuario;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.valueobject.UsuarioId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ReconciliarIdentidadService implements ReconciliarIdentidadUseCase {

    private final UsuarioRepository usuarioRepository;
    private final IdentityProvisioningPort identityProvisioningPort;

    public ReconciliarIdentidadService(
            UsuarioRepository usuarioRepository,
            IdentityProvisioningPort identityProvisioningPort
    ) {
        this.usuarioRepository = Objects.requireNonNull(usuarioRepository, "usuarioRepository no puede ser null.");
        this.identityProvisioningPort = Objects.requireNonNull(identityProvisioningPort, "identityProvisioningPort no puede ser null.");
    }

    @Override
    public UsuarioResponse ejecutar(UUID empresaId, UUID usuarioId) {
        EmpresaId tenant = EmpresaId.de(empresaId);
        UsuarioId id = UsuarioId.de(usuarioId);
        Usuario usuario = usuarioRepository.buscarPorId(tenant, id)
                .orElseThrow(() -> new UsuarioNoEncontradoException(id, tenant));

        identityProvisioningPort.reconciliar(usuario);
        return UsuarioApplicationMapper.toResponse(usuario);
    }
}
