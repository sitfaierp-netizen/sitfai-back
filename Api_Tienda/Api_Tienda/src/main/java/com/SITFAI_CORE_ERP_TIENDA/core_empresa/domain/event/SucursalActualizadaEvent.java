package com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.event;

import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.valueobject.SucursalId;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record SucursalActualizadaEvent(
        UUID eventoId,
        Instant ocurridoEn,
        EmpresaId empresaId,
        SucursalId sucursalId,
        String codigo,
        String nombre
) implements DomainEvent {

    public SucursalActualizadaEvent {
        Objects.requireNonNull(eventoId);
        Objects.requireNonNull(ocurridoEn);
        Objects.requireNonNull(empresaId);
        Objects.requireNonNull(sucursalId);
        Objects.requireNonNull(codigo);
        Objects.requireNonNull(nombre);
    }

    public static SucursalActualizadaEvent ahora(EmpresaId empresaId, SucursalId sucursalId, String codigo, String nombre) {
        return new SucursalActualizadaEvent(UUID.randomUUID(), Instant.now(), empresaId, sucursalId, codigo, nombre);
    }
}
