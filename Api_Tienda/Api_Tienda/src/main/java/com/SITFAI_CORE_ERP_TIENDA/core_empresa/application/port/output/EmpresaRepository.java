package com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.port.output;

import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.model.Empresa;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.valueobject.Ruc;

import java.util.Optional;

public interface EmpresaRepository {
    boolean existePorRuc(Ruc ruc);
    Empresa guardar(Empresa empresa);
    Optional<Empresa> buscarPorId(EmpresaId id);
    Optional<Empresa> buscarPorRuc(Ruc ruc);
    java.util.List<Empresa> buscarTodas();
    boolean existe(EmpresaId id);
}
