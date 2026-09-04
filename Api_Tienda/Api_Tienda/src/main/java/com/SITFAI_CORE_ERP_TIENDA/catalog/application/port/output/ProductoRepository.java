package com.SITFAI_CORE_ERP_TIENDA.catalog.application.port.output;

import com.SITFAI_CORE_ERP_TIENDA.catalog.domain.model.Producto;
import com.SITFAI_CORE_ERP_TIENDA.catalog.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.catalog.domain.valueobject.ProductoId;

import java.util.List;
import java.util.Optional;

/**
 * Driven Port: Contrato de persistencia para el Aggregate Root Producto.
 * Implementado por ProductoJpaAdapter en Infrastructure.
 */
public interface ProductoRepository {
    void guardar(Producto producto);
    Optional<Producto> buscarPorIdYEmpresa(ProductoId id, EmpresaId empresaId);
    List<Producto> listarPorEmpresa(EmpresaId empresaId);
    boolean existeCodigoBarras(String codigoBarras, EmpresaId empresaId, ProductoId excluyendoProductoId);
}
