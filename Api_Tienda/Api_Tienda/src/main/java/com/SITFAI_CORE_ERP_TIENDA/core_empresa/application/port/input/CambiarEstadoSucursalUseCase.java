package com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.port.input;

import com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.dto.SucursalResponse;

import java.util.UUID;

/**
 * Puerto de Entrada (Driving Port): Caso de Uso para alternar el estado de una Sucursal.
 * Implementa la Regla de Negocio SUC-04: Una Sucursal puede estar ACTIVA o INACTIVA.
 */
public interface CambiarEstadoSucursalUseCase {

    /**
     * Alterna el estado de la Sucursal entre ACTIVA e INACTIVA (Regla SUC-04).
     * Si está ACTIVA → pasa a INACTIVA. Si está INACTIVA → pasa a ACTIVA.
     * La activación requiere que la Empresa esté ACTIVA (Regla EMP-06).
     *
     * @param empresaId  UUID de la Empresa propietaria (validación de tenancy — Regla MT-02).
     * @param sucursalId UUID de la Sucursal a modificar.
     * @return {@link SucursalResponse} con el nuevo estado de la Sucursal.
     */
    SucursalResponse ejecutar(UUID empresaId, UUID sucursalId);
}
