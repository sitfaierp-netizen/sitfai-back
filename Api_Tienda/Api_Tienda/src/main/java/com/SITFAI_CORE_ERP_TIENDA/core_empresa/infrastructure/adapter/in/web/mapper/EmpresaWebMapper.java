package com.SITFAI_CORE_ERP_TIENDA.core_empresa.infrastructure.adapter.in.web.mapper;

import com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.dto.AgregarSucursalCommand;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.dto.DarDeBajaEmpresaCommand;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.dto.EmpresaResponse;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.dto.RegistrarEmpresaCommand;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.dto.SucursalResponse;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.dto.SuspenderEmpresaCommand;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.infrastructure.adapter.in.web.dto.AgregarSucursalRequest;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.infrastructure.adapter.in.web.dto.CambiarEstadoEmpresaRequest;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.infrastructure.adapter.in.web.dto.EmpresaWebResponse;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.infrastructure.adapter.in.web.dto.RegistrarEmpresaRequest;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.infrastructure.adapter.in.web.dto.SucursalWebResponse;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Mapper estático entre Web Requests/Responses y DTOs de Aplicación (Regla 1 y Regla 5).
 */
public final class EmpresaWebMapper {

    private EmpresaWebMapper() {
        // Utility class
    }

    public static RegistrarEmpresaCommand toCommand(RegistrarEmpresaRequest request) {
        if (request == null) return null;
        return new RegistrarEmpresaCommand(
                request.ruc(),
                request.nombre()
        );
    }

    public static AgregarSucursalCommand toCommand(UUID empresaId, AgregarSucursalRequest request) {
        if (request == null) return null;
        return new AgregarSucursalCommand(
                empresaId,
                request.codigo(),
                request.nombre()
        );
    }

    public static SuspenderEmpresaCommand toSuspenderCommand(UUID empresaId, CambiarEstadoEmpresaRequest request) {
        if (request == null) return null;
        return new SuspenderEmpresaCommand(
                empresaId,
                request.motivo() != null ? request.motivo() : "Suspensión administrativa"
        );
    }

    public static DarDeBajaEmpresaCommand toDarDeBajaCommand(UUID empresaId, CambiarEstadoEmpresaRequest request) {
        if (request == null) return null;
        return new DarDeBajaEmpresaCommand(
                empresaId,
                request.motivo() != null ? request.motivo() : "Baja definitiva"
        );
    }

    public static EmpresaWebResponse toWebResponse(EmpresaResponse response) {
        if (response == null) return null;

        List<SucursalWebResponse> sucursalesWeb = response.sucursales() != null
                ? response.sucursales().stream().map(EmpresaWebMapper::toWebResponse).toList()
                : Collections.emptyList();

        return new EmpresaWebResponse(
                response.id(),
                response.ruc(),
                response.nombre(),
                response.estado(),
                sucursalesWeb,
                response.creadoEn(),
                response.actualizadoEn()
        );
    }

    public static SucursalWebResponse toWebResponse(SucursalResponse response) {
        if (response == null) return null;
        return new SucursalWebResponse(
                response.id(),
                response.codigo(),
                response.nombre(),
                response.estado(),
                response.creadoEn(),
                response.actualizadoEn()
        );
    }

    public static List<EmpresaWebResponse> toWebResponseList(List<EmpresaResponse> list) {
        if (list == null) return Collections.emptyList();
        return list.stream().map(EmpresaWebMapper::toWebResponse).toList();
    }
}
