package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.port.output;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.despacho.Despacho;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.despacho.vo.DespachoId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.despacho.vo.PedidoId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.EmpresaId;

import java.util.List;
import java.util.Optional;

/**
 * Output Port (Driven Port): Repositorio del Agregado Despacho (Outbound Logistics).
 * <p>
 * Interfaz pura de Java (Arquitectura Hexagonal / Clean Architecture).
 * Regla 1: Cero dependencias de JPA, Spring Data o cualquier framework técnico.
 * Regla MT-01: Exige EmpresaId en cada firma de método para blindar el aislamiento multitenant.
 */
public interface DespachoRepository {

    /**
     * Persiste o actualiza un Agregado Despacho dentro del tenant especificado.
     *
     * @param despacho Agregado a persistir.
     * @param empresaId Tenant del usuario (MT-01).
     * @return El Agregado persistido.
     */
    Despacho guardar(Despacho despacho, EmpresaId empresaId);

    /**
     * Busca un despacho por su identificador único dentro del tenant.
     *
     * @param id Identificador del despacho.
     * @param empresaId Tenant del usuario (MT-01).
     * @return Optional con el Despacho si existe y pertenece al tenant.
     */
    Optional<Despacho> buscarPorId(DespachoId id, EmpresaId empresaId);

    /**
     * Busca un despacho por su Pedido de origen (BOD-04 / E-commerce).
     *
     * @param pedidoId Identificador del pedido de origen.
     * @param empresaId Tenant del usuario (MT-01).
     * @return Optional con el Despacho si existe en el tenant.
     */
    Optional<Despacho> buscarPorPedidoId(PedidoId pedidoId, EmpresaId empresaId);

    /**
     * Lista todos los despachos pertenecientes al tenant.
     *
     * @param empresaId Tenant del usuario (MT-01).
     * @return Lista de despachos del tenant.
     */
    List<Despacho> listarPorEmpresa(EmpresaId empresaId);
}
