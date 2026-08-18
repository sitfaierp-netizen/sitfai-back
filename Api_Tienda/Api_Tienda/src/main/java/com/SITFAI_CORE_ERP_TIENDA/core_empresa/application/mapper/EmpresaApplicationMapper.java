package com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.mapper;

import com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.dto.EmpresaResponse;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.dto.SucursalResponse;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.model.Empresa;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.model.Sucursal;

import java.util.Collections;
import java.util.List;

/**
 * Mapeador estático entre el Dominio de Core-Empresa y los DTOs de Aplicación (Regla 1).
 */
public final class EmpresaApplicationMapper {

    private EmpresaApplicationMapper() {
        // Utility class
    }

    public static EmpresaResponse toResponse(Empresa empresa) {
        if (empresa == null) {
            return null;
        }

        List<SucursalResponse> sucursalesDto = empresa.getSucursales() != null
                ? empresa.getSucursales().stream().map(EmpresaApplicationMapper::toResponse).toList()
                : Collections.emptyList();

        return new EmpresaResponse(
                empresa.getId().valor(),
                empresa.getRuc().valor(),
                empresa.getNombre().valor(),
                empresa.getEstado().name(),
                sucursalesDto,
                empresa.getCreadoEn(),
                empresa.getActualizadoEn()
        );
    }

    public static SucursalResponse toResponse(Sucursal sucursal) {
        if (sucursal == null) {
            return null;
        }

        return new SucursalResponse(
                sucursal.getId().valor(),
                sucursal.getCodigo(),
                sucursal.getNombre(),
                sucursal.getEstado().name(),
                sucursal.getCreadoEn(),
                sucursal.getActualizadoEn()
        );
    }

    public static List<EmpresaResponse> toResponseList(List<Empresa> empresas) {
        if (empresas == null) {
            return Collections.emptyList();
        }
        return empresas.stream().map(EmpresaApplicationMapper::toResponse).toList();
    }
}
