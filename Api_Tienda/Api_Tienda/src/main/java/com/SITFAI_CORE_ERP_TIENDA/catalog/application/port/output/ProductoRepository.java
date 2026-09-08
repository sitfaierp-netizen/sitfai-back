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
    java.util.Optional<Producto> buscarPorIdYEmpresa(ProductoId id, EmpresaId empresaId);
    org.springframework.data.domain.Page<Producto> listarPorEmpresa(EmpresaId empresaId, org.springframework.data.domain.Pageable pageable);
    boolean existeCodigoBarras(String codigoBarras, EmpresaId empresaId, ProductoId excluyendoProductoId);
    void deleteById(ProductoId id);
}
