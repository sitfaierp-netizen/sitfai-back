package com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.port.input;

import com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.dto.EmpresaResponse;

import java.util.List;
import java.util.UUID;

/**
 * Puerto de Entrada (Driving Port): Caso de Uso para consultas y lectura de Empresas.
 */
public interface ConsultarEmpresaUseCase {

    EmpresaResponse obtenerPorId(UUID empresaId);

    EmpresaResponse obtenerPorRuc(String ruc);

    List<EmpresaResponse> listarTodas();
}
