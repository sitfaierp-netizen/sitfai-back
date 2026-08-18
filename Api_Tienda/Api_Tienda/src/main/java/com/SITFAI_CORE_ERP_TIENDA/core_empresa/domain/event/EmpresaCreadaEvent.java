package com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.event;

import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.valueobject.NombreEmpresa;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.valueobject.Ruc;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.valueobject.SucursalId;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Evento de dominio emitido cuando una nueva Empresa (Tenant Raíz) es registrada en el sistema.
 */
public record EmpresaCreadaEvent(
        UUID eventoId,
        EmpresaId empresaId,
        Ruc ruc,
        NombreEmpresa nombre,
        SucursalId sucursalPrincipalId,
        String codigoSucursalPrincipal,
        Instant ocurridoEn
) implements DomainEvent {

    public EmpresaCreadaEvent {
        Objects.requireNonNull(eventoId, "eventoId no puede ser null.");
        Objects.requireNonNull(empresaId, "empresaId no puede ser null.");
        Objects.requireNonNull(ruc, "ruc no puede ser null.");
        Objects.requireNonNull(nombre, "nombre no puede ser null.");
        Objects.requireNonNull(sucursalPrincipalId, "sucursalPrincipalId no puede ser null.");
        Objects.requireNonNull(ocurridoEn, "ocurridoEn no puede ser null.");
    }

    public static EmpresaCreadaEvent ahora(
            EmpresaId empresaId,
            Ruc ruc,
            NombreEmpresa nombre,
            SucursalId sucursalPrincipalId,
            String codigoSucursalPrincipal
    ) {
        return new EmpresaCreadaEvent(
                UUID.randomUUID(),
                empresaId,
                ruc,
                nombre,
                sucursalPrincipalId,
                codigoSucursalPrincipal,
                Instant.now()
        );
    }
}
