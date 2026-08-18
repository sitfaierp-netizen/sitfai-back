package com.SITFAI_CORE_ERP_TIENDA.iam_module.application.service;

import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.dto.UsuarioResponse;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.mapper.UsuarioApplicationMapper;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.port.input.ConsultarUsuarioUseCase;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.port.output.UsuarioRepository;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.exception.UsuarioNoEncontradoException;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.model.Usuario;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.valueobject.Username;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.valueobject.UsuarioId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Servicio de Aplicación: Consultas de usuarios respetando estrictamente MT-01.
 */
@Service
@Transactional(readOnly = true)
public class ConsultarUsuarioService implements ConsultarUsuarioUseCase {

    private final UsuarioRepository usuarioRepository;

    public ConsultarUsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = Objects.requireNonNull(usuarioRepository, "usuarioRepository no puede ser null.");
    }

    @Override
    public UsuarioResponse obtenerPorId(UUID empresaId, UUID id) {
        EmpresaId eId = EmpresaId.de(empresaId);
        UsuarioId uId = UsuarioId.de(id);

        Usuario usuario = usuarioRepository.buscarPorId(eId, uId)
                .orElseThrow(() -> new UsuarioNoEncontradoException(uId, eId));

        return UsuarioApplicationMapper.toResponse(usuario);
    }

    @Override
    public UsuarioResponse obtenerPorUsername(UUID empresaId, String username) {
        EmpresaId eId = EmpresaId.de(empresaId);
        Username uName = Username.de(username);

        Usuario usuario = usuarioRepository.buscarPorUsername(eId, uName)
                .orElseThrow(() -> new UsuarioNoEncontradoException("Usuario '" + username + "' no encontrado para la empresa."));

        return UsuarioApplicationMapper.toResponse(usuario);
    }

    @Override
    public List<UsuarioResponse> listarPorEmpresa(UUID empresaId) {
        EmpresaId eId = EmpresaId.de(empresaId);
        List<Usuario> list = usuarioRepository.buscarPorEmpresa(eId);
        return UsuarioApplicationMapper.toResponseList(list);
    }
}
