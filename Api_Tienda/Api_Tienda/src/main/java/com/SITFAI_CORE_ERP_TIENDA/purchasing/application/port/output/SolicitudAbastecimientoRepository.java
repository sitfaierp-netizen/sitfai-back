package com.SITFAI_CORE_ERP_TIENDA.purchasing.application.port.output;

import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.SolicitudAbastecimiento;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.valueobject.SolicitudId;

import java.util.Optional;

/**
 * Driven Port (Output): Contrato de persistencia para SolicitudAbastecimiento.
 * Implementado por SolicitudJpaAdapter en la capa de infraestructura.
 * El dominio no conoce JPA ni ningún ORM (Regla 1, Regla 6).
 */
public interface SolicitudAbastecimientoRepository {

    /** Persiste una solicitud (nueva o actualizada). */
    void guardar(SolicitudAbastecimiento solicitud);

    /**
     * Busca una solicitud por su ID y empresa (MT-01 — aislamiento de tenant).
     *
     * @param id        Identidad de la solicitud.
     * @param empresaId Tenant del contexto de seguridad.
     * @return          La solicitud si existe y pertenece al tenant.
     */
    Optional<SolicitudAbastecimiento> buscarPorIdYEmpresa(SolicitudId id, EmpresaId empresaId);
}
