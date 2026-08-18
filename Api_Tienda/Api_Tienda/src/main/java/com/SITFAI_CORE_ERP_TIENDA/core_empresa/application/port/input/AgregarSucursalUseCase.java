package com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.port.input;

import com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.dto.AgregarSucursalCommand;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.dto.SucursalResponse;

/**
 * Puerto de Entrada (Driving Port): Caso de Uso para agregar una sucursal a una Empresa existente (SUC-01, SUC-02).
 */
public interface AgregarSucursalUseCase {

    SucursalResponse ejecutar(AgregarSucursalCommand command);
}
