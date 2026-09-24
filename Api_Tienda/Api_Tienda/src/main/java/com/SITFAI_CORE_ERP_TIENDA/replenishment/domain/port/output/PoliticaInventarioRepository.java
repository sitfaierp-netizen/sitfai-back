package com.SITFAI_CORE_ERP_TIENDA.replenishment.domain.port.output;

import com.SITFAI_CORE_ERP_TIENDA.replenishment.domain.model.politica.PoliticaInventario;
import com.SITFAI_CORE_ERP_TIENDA.replenishment.domain.model.politica.vo.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.replenishment.domain.model.politica.vo.PoliticaId;

import java.util.Optional;

public interface PoliticaInventarioRepository {
    PoliticaInventario guardar(PoliticaInventario politica);
    Optional<PoliticaInventario> buscarPorId(PoliticaId id, EmpresaId empresaId);
}
