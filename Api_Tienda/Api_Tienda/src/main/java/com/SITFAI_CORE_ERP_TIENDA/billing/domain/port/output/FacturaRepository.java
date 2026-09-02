package com.SITFAI_CORE_ERP_TIENDA.billing.domain.port.output;

import com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.Factura;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.vo.FacturaId;

import java.util.Optional;
import java.util.UUID;

public interface FacturaRepository {
    void save(Factura factura);
    Optional<Factura> findByIdAndEmpresaId(FacturaId id, UUID empresaId);
}
