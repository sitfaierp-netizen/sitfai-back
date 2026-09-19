package com.SITFAI_CORE_ERP_TIENDA.pos.application.port.input;

import com.SITFAI_CORE_ERP_TIENDA.pos.application.query.dto.CajaView;
import java.util.List;
import java.util.UUID;

public interface ConsultarCajasUseCase {
    List<CajaView> listarCajasPorEmpresa(UUID empresaId);
}
