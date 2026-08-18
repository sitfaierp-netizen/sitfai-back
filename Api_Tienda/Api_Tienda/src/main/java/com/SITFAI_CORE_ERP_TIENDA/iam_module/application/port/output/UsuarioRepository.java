package com.SITFAI_CORE_ERP_TIENDA.iam_module.application.port.output;

import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.model.Usuario;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.valueobject.Email;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.valueobject.Username;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.valueobject.UsuarioId;

import java.util.List;
import java.util.Optional;

/**
 * Driven Port / Output Port: Contrato de persistencia para el Agregado Usuario.
 * Cumple con MT-01 requiriendo obligatoriamente EmpresaId en búsquedas multitenant.
 */
public interface UsuarioRepository {

    /**
     * Guarda o actualiza el Agregado Usuario.
     */
    Usuario guardar(Usuario usuario);

    /**
     * Busca un usuario por ID validando el tenant (MT-01).
     */
    Optional<Usuario> buscarPorId(EmpresaId empresaId, UsuarioId id);

    /**
     * Busca un usuario por Username dentro del tenant.
     */
    Optional<Usuario> buscarPorUsername(EmpresaId empresaId, Username username);

    /**
     * Busca un usuario por Email dentro del tenant.
     */
    Optional<Usuario> buscarPorEmail(EmpresaId empresaId, Email email);

    /**
     * Verifica la existencia de un username en el tenant.
     */
    boolean existePorUsername(EmpresaId empresaId, Username username);

    /**
     * Verifica la existencia de un email en el tenant.
     */
    boolean existePorEmail(EmpresaId empresaId, Email email);

    /**
     * Lista todos los usuarios pertenecientes al tenant.
     */
    List<Usuario> buscarPorEmpresa(EmpresaId empresaId);
}
