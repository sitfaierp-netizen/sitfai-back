package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.port.input;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.query.dto.BodegaView;
import java.util.List;

public interface ConsultarBodegasUseCase {
    List<BodegaView> listarBodegasPorEmpresa();
}
