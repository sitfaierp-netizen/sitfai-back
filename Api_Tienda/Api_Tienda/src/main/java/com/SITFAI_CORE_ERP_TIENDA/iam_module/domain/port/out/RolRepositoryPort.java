package com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.port.out;

import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.model.Rol;
import java.util.List;
import java.util.Optional;

public interface RolRepositoryPort {
    Rol guardar(Rol rol);
    Optional<Rol> buscarPorId(String id);
    Optional<Rol> buscarPorCodigo(String codigo);
    List<Rol> listarTodos();
}
