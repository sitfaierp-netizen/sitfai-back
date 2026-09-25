package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.domain.port.output;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.domain.model.bom.ListaMateriales;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.domain.model.bom.vo.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.domain.model.bom.vo.RecetaId;

import java.util.Optional;

public interface ListaMaterialesRepository {
    void guardar(ListaMateriales listaMateriales, EmpresaId empresaId);
    Optional<ListaMateriales> buscarPorId(RecetaId id, EmpresaId empresaId);
}
