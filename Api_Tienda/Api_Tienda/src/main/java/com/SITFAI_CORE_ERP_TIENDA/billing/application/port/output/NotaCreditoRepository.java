package com.SITFAI_CORE_ERP_TIENDA.billing.application.port.output;

import com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.NotaCreditoElectronica;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.valueobject.NotaCreditoId;

import java.util.Optional;

/**
 * Driven Port: Repositorio de Notas de Crédito.
 * Exige EmpresaId en las búsquedas (Regla MT-01).
 */
public interface NotaCreditoRepository {
    void guardar(NotaCreditoElectronica nota);
    Optional<NotaCreditoElectronica> buscarPorId(NotaCreditoId id, EmpresaId empresaId);
}
