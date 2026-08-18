package com.SITFAI_CORE_ERP_TIENDA.billing.application.port.output;

import com.SITFAI_CORE_ERP_TIENDA.billing.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.valueobject.ResolucionDian;
import java.util.Optional;

/**
 * Driven Port: Obtener la resolución activa.
 */
public interface ResolucionRepository {
    Optional<ResolucionDian> obtenerActiva(EmpresaId empresaId);
}
