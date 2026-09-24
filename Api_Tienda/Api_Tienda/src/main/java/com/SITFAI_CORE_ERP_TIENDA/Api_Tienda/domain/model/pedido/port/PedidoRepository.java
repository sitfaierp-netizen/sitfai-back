package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.pedido.port;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.pedido.Pedido;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.pedido.vo.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.pedido.vo.PedidoId;

import java.util.List;
import java.util.Optional;

/**
 * Output Port (Driven Port): Contrato de persistencia para el Agregado {@link Pedido}.
 * <p>
 * Regla REGLA-1: Interfaz pura de Java sin imports de frameworks, Spring ni JPA.
 * Regla MT-01: Exige {@link EmpresaId} en cada firma de método para blindar el particionamiento multi-tenant.
 */
public interface PedidoRepository {

    /**
     * Persiste o actualiza el Agregado {@link Pedido} junto con sus líneas.
     *
     * @param pedido Agregado a persistir.
     */
    void guardar(Pedido pedido);

    /**
     * Recupera un pedido por su ID asegurando el aislamiento multi-inquilino.
     *
     * @param empresaId Tenant raíz autenticado (MT-01).
     * @param id        Identificador del pedido.
     * @return El pedido si existe y pertenece a la empresa.
     */
    Optional<Pedido> buscarPorId(EmpresaId empresaId, PedidoId id);

    /**
     * Lista todos los pedidos pertenecientes a una empresa.
     *
     * @param empresaId Tenant raíz autenticado (MT-01).
     * @return Lista de pedidos del tenant.
     */
    List<Pedido> listarPorEmpresa(EmpresaId empresaId);

    /**
     * Verifica la existencia de un pedido en un tenant específico.
     *
     * @param empresaId Tenant raíz autenticado (MT-01).
     * @param id        Identificador del pedido.
     * @return true si existe bajo ese tenant.
     */
    boolean existePorId(EmpresaId empresaId, PedidoId id);
}
