package com.SITFAI_CORE_ERP_TIENDA.billing.application.port.input;

import com.SITFAI_CORE_ERP_TIENDA.billing.application.dto.FacturaResponse;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.vo.FacturaId;

import java.util.UUID;

/**
 * Driving Port: Caso de Uso para Consultar una Factura (MT-01).
 */
public interface ConsultarFacturaUseCase {
    FacturaResponse consultarPorId(FacturaId id, UUID empresaId);
}
