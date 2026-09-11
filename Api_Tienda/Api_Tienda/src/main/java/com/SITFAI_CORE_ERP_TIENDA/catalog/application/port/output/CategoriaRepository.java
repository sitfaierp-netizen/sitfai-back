package com.SITFAI_CORE_ERP_TIENDA.catalog.application.port.output;

import com.SITFAI_CORE_ERP_TIENDA.catalog.domain.model.Categoria;
import com.SITFAI_CORE_ERP_TIENDA.catalog.domain.valueobject.EmpresaId;

import java.util.List;

public interface CategoriaRepository {
    void guardar(Categoria categoria);
    List<Categoria> obtenerTodasPorEmpresa(EmpresaId empresaId);
}
