package com.SITFAI_CORE_ERP_TIENDA.pos.domain.port.output;

import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.TicketVenta;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.vo.TicketId;

import java.util.Optional;
import java.util.UUID;

/**
 * Output Port (Puerto de Salida): Repositorio de Tickets de Venta POS.
 *
 * Interfaz pura de Java. Cero dependencias a JPA, Spring o cualquier framework.
 * La implementación concreta vive en la capa de Infraestructura (Adapter Out).
 */
public interface TicketVentaRepository {

    /**
     * Persiste un nuevo ticket o actualiza uno existente.
     */
    void save(TicketVenta ticket);

    /**
     * Busca un ticket por su ID dentro del contexto del tenant (MT-01).
     */
    Optional<TicketVenta> findByIdAndEmpresaId(TicketId id, UUID empresaId);
}
