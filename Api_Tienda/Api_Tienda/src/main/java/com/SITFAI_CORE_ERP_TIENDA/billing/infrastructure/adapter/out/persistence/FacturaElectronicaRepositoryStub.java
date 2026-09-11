package com.SITFAI_CORE_ERP_TIENDA.billing.infrastructure.adapter.out.persistence;

import com.SITFAI_CORE_ERP_TIENDA.billing.application.port.output.FacturaRepository;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.FacturaElectronica;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.valueobject.FacturaId;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.valueobject.PedidoOrigenId;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class FacturaElectronicaRepositoryStub implements FacturaRepository {

    @Override
    public void guardar(FacturaElectronica factura) {
        throw new UnsupportedOperationException("Stub: Not implemented yet");
    }

    @Override
    public Optional<FacturaElectronica> buscarPorId(FacturaId id, EmpresaId empresaId) {
        return Optional.empty();
    }

    @Override
    public Optional<FacturaElectronica> buscarPorPedidoOrigen(PedidoOrigenId pedidoOrigenId, EmpresaId empresaId) {
        return Optional.empty();
    }

    @Override
    public boolean existePorPedidoOrigen(PedidoOrigenId pedidoOrigenId, EmpresaId empresaId) {
        return false;
    }
}
