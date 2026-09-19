package com.SITFAI_CORE_ERP_TIENDA.pos.application.port.output.query;

import com.SITFAI_CORE_ERP_TIENDA.pos.application.query.dto.CajaView;
import java.util.List;
import java.util.UUID;

public interface PosQueryRepository {
    List<CajaView> findCajasByEmpresa(UUID empresaId);
}
