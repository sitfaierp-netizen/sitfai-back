package com.SITFAI_CORE_ERP_TIENDA.replenishment.application.port.input;

import com.SITFAI_CORE_ERP_TIENDA.replenishment.application.dto.GestionarPoliticaInventarioCommand;
import com.SITFAI_CORE_ERP_TIENDA.replenishment.application.dto.PoliticaInventarioResult;

public interface GestionarPoliticaInventarioUseCase {
    PoliticaInventarioResult crear(GestionarPoliticaInventarioCommand command);

    PoliticaInventarioResult actualizar(GestionarPoliticaInventarioCommand command);
}
