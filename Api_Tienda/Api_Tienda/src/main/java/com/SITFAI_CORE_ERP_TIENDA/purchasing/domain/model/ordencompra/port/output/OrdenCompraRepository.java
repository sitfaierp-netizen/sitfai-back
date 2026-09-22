package com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.ordencompra.port.output;

import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.ordencompra.OrdenCompra;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.ordencompra.vo.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.ordencompra.vo.EstadoOrdenCompra;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.ordencompra.vo.OrdenCompraId;

import java.util.List;
import java.util.Optional;

/**
 * Output Port: Repositorio de persistencia del Agregado {@link OrdenCompra} (BOD-04).
 * <p>
 * Regla MT-01: Exige {@link EmpresaId} en todas las firmas de consulta para erradicar cualquier fuga inter-tenant.
 * Pure Java Interface: Cero imports de Spring, JPA o Hibernate (Regla 1).
 */
public interface OrdenCompraRepository {

    /**
     * Persiste o actualiza el estado de la Orden de Compra.
     */
    OrdenCompra guardar(OrdenCompra ordenCompra);

    /**
     * Busca una Orden de Compra por su ID y el Tenant propietario (MT-01).
     */
    Optional<OrdenCompra> buscarPorId(OrdenCompraId id, EmpresaId empresaId);

    /**
     * Lista todas las Órdenes de Compra del Tenant (MT-01).
     */
    List<OrdenCompra> buscarPorEmpresa(EmpresaId empresaId);

    /**
     * Lista las Órdenes de Compra de un Tenant filtradas por estado lógico.
     */
    List<OrdenCompra> buscarPorEmpresaYEstado(EmpresaId empresaId, EstadoOrdenCompra estado);
}
