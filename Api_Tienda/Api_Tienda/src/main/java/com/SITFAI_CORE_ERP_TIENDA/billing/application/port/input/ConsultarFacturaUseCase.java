package com.SITFAI_CORE_ERP_TIENDA.billing.application.port.input;

import com.SITFAI_CORE_ERP_TIENDA.billing.application.dto.FacturaResponse;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.valueobject.FacturaId;

/**
 * Driving Port: Caso de Uso para Consultar una Factura.
 */
public interface ConsultarFacturaUseCase {
    FacturaResponse consultarPorId(FacturaId id, EmpresaId empresaId);
}
