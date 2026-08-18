package com.SITFAI_CORE_ERP_TIENDA.sourcing.application.port.output;

import com.SITFAI_CORE_ERP_TIENDA.sourcing.domain.model.Proveedor;
import com.SITFAI_CORE_ERP_TIENDA.sourcing.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.sourcing.domain.valueobject.ProveedorId;

import java.util.Optional;

/** Driven Port de persistencia. */
public interface ProveedorRepository {
    void guardar(Proveedor proveedor);
    Optional<Proveedor> buscarPorIdYEmpresa(ProveedorId id, EmpresaId empresaId);
}
