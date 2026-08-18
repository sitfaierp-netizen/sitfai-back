package com.SITFAI_CORE_ERP_TIENDA.billing.application.port.output;

import com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.FacturaElectronica;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.valueobject.FacturaId;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.valueobject.PedidoOrigenId;

import java.util.Optional;

/**
 * Driven Port: Repositorio de Facturas Electrónicas.
 * Exige EmpresaId en las búsquedas (Regla MT-01).
 */
public interface FacturaRepository {
    void guardar(FacturaElectronica factura);
    Optional<FacturaElectronica> buscarPorId(FacturaId id, EmpresaId empresaId);
    Optional<FacturaElectronica> buscarPorPedidoOrigen(PedidoOrigenId pedidoOrigenId, EmpresaId empresaId);
    boolean existePorPedidoOrigen(PedidoOrigenId pedidoOrigenId, EmpresaId empresaId);
}
