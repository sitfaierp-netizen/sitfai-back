package com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.event;

import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.valueobject.SucursalId;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Evento de dominio emitido cuando una nueva Sucursal es agregada a la Empresa (SUC-01, SUC-02).
 */
public record SucursalAgregadaEvent(
        UUID eventoId,
        EmpresaId empresaId,
        SucursalId sucursalId,
        String codigoSucursal,
        String nombreSucursal,
        Instant ocurridoEn
) implements DomainEvent {

    public SucursalAgregadaEvent {
        Objects.requireNonNull(eventoId, "eventoId no puede ser null.");
        Objects.requireNonNull(empresaId, "empresaId no puede ser null.");
        Objects.requireNonNull(sucursalId, "sucursalId no puede ser null.");
        Objects.requireNonNull(codigoSucursal, "codigoSucursal no puede ser null.");
        Objects.requireNonNull(nombreSucursal, "nombreSucursal no puede ser null.");
        Objects.requireNonNull(ocurridoEn, "ocurridoEn no puede ser null.");
    }

    public static SucursalAgregadaEvent ahora(
            EmpresaId empresaId,
            SucursalId sucursalId,
            String codigoSucursal,
            String nombreSucursal
    ) {
        return new SucursalAgregadaEvent(
                UUID.randomUUID(),
                empresaId,
                sucursalId,
                codigoSucursal,
                nombreSucursal,
                Instant.now()
        );
    }
}
