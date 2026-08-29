package com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.port.output;

import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.model.Sucursal;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.valueobject.SucursalId;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de Salida (Driven Port): Contrato de persistencia para la entidad Sucursal.
 * Permite consultar y actualizar Sucursales de forma aislada por Empresa (Regla MT-02).
 */
public interface SucursalRepository {

    /**
     * Retorna todas las Sucursales que pertenecen a la Empresa indicada (Regla MT-02 / SUC-01).
     *
     * @param empresaId discriminador del tenant.
     * @return lista de Sucursales, vacía si ninguna pertenece a esa Empresa.
     */
    List<Sucursal> buscarPorEmpresaId(EmpresaId empresaId);

    /**
     * Busca una Sucursal específica validando que pertenezca a la Empresa indicada (Regla MT-02).
     *
     * @param sucursalId identificador de la Sucursal.
     * @param empresaId  identificador de la Empresa propietaria.
     * @return {@link Optional} con la Sucursal, vacío si no existe o no pertenece al tenant.
     */
    Optional<Sucursal> buscarPorIdYEmpresaId(SucursalId sucursalId, EmpresaId empresaId);

    /**
     * Persiste el estado actualizado de una Sucursal.
     *
     * @param sucursal   la Sucursal con su nuevo estado.
     * @param empresaId  la Empresa propietaria (para mantener la FK).
     * @return la Sucursal guardada reconstituida desde la BD.
     */
    Sucursal guardar(Sucursal sucursal, EmpresaId empresaId);

    /**
     * Verifica si ya existe una Sucursal con el mismo código en la Empresa.
     * @param empresaId Identificador del tenant
     * @param codigo Código de sucursal
     * @return true si existe
     */
    boolean existePorEmpresaIdYCodigo(EmpresaId empresaId, String codigo);
}
