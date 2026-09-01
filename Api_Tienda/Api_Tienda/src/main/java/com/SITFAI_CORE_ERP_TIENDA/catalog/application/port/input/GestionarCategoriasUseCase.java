package com.SITFAI_CORE_ERP_TIENDA.catalog.application.port.input;

import com.SITFAI_CORE_ERP_TIENDA.catalog.application.dto.CategoriaResponse;
import com.SITFAI_CORE_ERP_TIENDA.catalog.application.dto.CrearCategoriaCommand;

import java.util.List;
import java.util.UUID;

public interface GestionarCategoriasUseCase {
    CategoriaResponse crear(CrearCategoriaCommand command, UUID empresaId);
    List<CategoriaResponse> listarPorEmpresa(UUID empresaId);
}
