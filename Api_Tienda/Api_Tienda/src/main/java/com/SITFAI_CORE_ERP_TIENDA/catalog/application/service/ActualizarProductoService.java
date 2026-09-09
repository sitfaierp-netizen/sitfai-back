package com.SITFAI_CORE_ERP_TIENDA.catalog.application.service;

import com.SITFAI_CORE_ERP_TIENDA.catalog.application.port.input.ActualizarProductoRequest;
import com.SITFAI_CORE_ERP_TIENDA.catalog.application.port.input.ActualizarProductoUseCase;
import com.SITFAI_CORE_ERP_TIENDA.catalog.application.port.output.ProductoRepository;
import com.SITFAI_CORE_ERP_TIENDA.catalog.application.port.output.TenantProviderPort;
import com.SITFAI_CORE_ERP_TIENDA.catalog.domain.exception.DomainException;
import com.SITFAI_CORE_ERP_TIENDA.catalog.domain.model.Producto;
import com.SITFAI_CORE_ERP_TIENDA.catalog.domain.valueobject.CategoriaId;
import com.SITFAI_CORE_ERP_TIENDA.catalog.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.catalog.domain.valueobject.Impuesto;
import com.SITFAI_CORE_ERP_TIENDA.catalog.domain.valueobject.ProductoId;
import com.SITFAI_CORE_ERP_TIENDA.catalog.domain.valueobject.UnidadMedida;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class ActualizarProductoService implements ActualizarProductoUseCase {

    private final ProductoRepository productoRepository;
    private final TenantProviderPort tenantProvider;

    public ActualizarProductoService(ProductoRepository productoRepository, TenantProviderPort tenantProvider) {
        this.productoRepository = productoRepository;
        this.tenantProvider = tenantProvider;
    }

    @Override
    public void actualizarProducto(UUID id, ActualizarProductoRequest request) {
        EmpresaId empresaId = EmpresaId.de(tenantProvider.obtenerEmpresaIdActual());
        ProductoId productoId = ProductoId.de(id.toString());

        Producto producto = productoRepository.buscarPorIdYEmpresa(productoId, empresaId)
                .orElseThrow(() -> new DomainException("Producto no encontrado o no pertenece a la empresa actual."));

        producto.actualizar(
                request.nombre(),
                request.descripcion(),
                CategoriaId.de(request.categoriaId().toString()),
                UnidadMedida.valueOf(request.unidadMedida()),
                request.precioCompra(),
                request.precioVenta(),
                Impuesto.valueOf(request.impuesto()),
                request.codigoBarras()
        );

        productoRepository.guardar(producto);
    }
}
