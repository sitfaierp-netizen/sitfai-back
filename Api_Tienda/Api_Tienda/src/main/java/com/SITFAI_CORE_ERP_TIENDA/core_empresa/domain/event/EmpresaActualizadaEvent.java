package com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.event;

import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.valueobject.NombreEmpresa;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.valueobject.Ruc;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record EmpresaActualizadaEvent(
        UUID eventoId,
        Instant ocurridoEn,
        EmpresaId empresaId,
        Ruc ruc,
        NombreEmpresa nombre
) implements DomainEvent {

    public EmpresaActualizadaEvent {
        Objects.requireNonNull(eventoId);
        Objects.requireNonNull(ocurridoEn);
        Objects.requireNonNull(empresaId);
        Objects.requireNonNull(ruc);
        Objects.requireNonNull(nombre);
    }

    public static EmpresaActualizadaEvent ahora(EmpresaId empresaId, Ruc ruc, NombreEmpresa nombre) {
        return new EmpresaActualizadaEvent(UUID.randomUUID(), Instant.now(), empresaId, ruc, nombre);
    }
}
