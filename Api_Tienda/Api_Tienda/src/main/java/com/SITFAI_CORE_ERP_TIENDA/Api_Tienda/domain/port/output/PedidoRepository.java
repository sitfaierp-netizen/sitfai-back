package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.port.output;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.pedido.Pedido;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.PedidoId;

import java.util.List;
import java.util.Optional;

/**
 * Driven Port (SPI): Contrato de persistencia para el Agregado {@link Pedido}.
 * <p>
 * Puro Java: Cero dependencias de Spring Data / JPA (REGLA 1, MCP-01).
 * Todos los métodos de consulta exigen {@link EmpresaId} para garantizar el aislamiento multi-inquilino (MT-01, MT-02).
 */
public interface PedidoRepository {

    /**
     * Guarda o actualiza el Agregado {@link Pedido} junto con todas sus líneas de detalle.
     */
    Pedido guardar(Pedido pedido);

    /**
     * Recupera un Pedido por su ID asegurando que pertenezca al Tenant indicado.
     */
    Optional<Pedido> buscarPorId(PedidoId id, EmpresaId empresaId);

    /**
     * Recupera todos los Pedidos asociados a un Tenant específico.
     */
    List<Pedido> buscarPorEmpresa(EmpresaId empresaId);

    /**
     * Verifica la existencia de un Pedido para un Tenant dado.
     */
    boolean existePorId(PedidoId id, EmpresaId empresaId);
}
