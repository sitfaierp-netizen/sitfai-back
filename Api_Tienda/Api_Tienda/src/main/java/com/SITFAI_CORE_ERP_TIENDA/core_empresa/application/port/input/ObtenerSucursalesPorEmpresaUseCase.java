package com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.port.input;

import com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.dto.SucursalResponse;

import java.util.List;
import java.util.UUID;

/**
 * Puerto de Entrada (Driving Port): Caso de Uso para consultar las Sucursales de una Empresa.
 * Garantiza el aislamiento multitenant (Regla MT-02): solo retorna sucursales
 * pertenecientes al empresaId indicado.
 */
public interface ObtenerSucursalesPorEmpresaUseCase {

    /**
     * Retorna la lista de Sucursales que pertenecen a la Empresa indicada (Regla SUC-01).
     *
     * @param empresaId UUID de la Empresa (tenant discriminador — Regla MT-02).
     * @return lista inmutable de {@link SucursalResponse}, vacía si no hay sucursales.
     */
    List<SucursalResponse> ejecutar(UUID empresaId);
}
