package com.SITFAI_CORE_ERP_TIENDA.catalog.application.service;

import com.SITFAI_CORE_ERP_TIENDA.catalog.application.dto.ProductoResponse;
import com.SITFAI_CORE_ERP_TIENDA.catalog.application.port.input.ConsultarProductosUseCase;
import com.SITFAI_CORE_ERP_TIENDA.catalog.application.port.output.ProductoRepository;
import com.SITFAI_CORE_ERP_TIENDA.catalog.application.port.output.TenantProviderPort;
import com.SITFAI_CORE_ERP_TIENDA.catalog.domain.model.Producto;
import com.SITFAI_CORE_ERP_TIENDA.catalog.domain.valueobject.EmpresaId;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ConsultarProductosService implements ConsultarProductosUseCase {

    private final ProductoRepository productoRepository;
    private final TenantProviderPort tenantProvider;

    public ConsultarProductosService(ProductoRepository productoRepository, TenantProviderPort tenantProvider) {
        this.productoRepository = productoRepository;
        this.tenantProvider = tenantProvider;
    }

    @Override
    public org.springframework.data.domain.Page<ProductoResponse> listarProductos(org.springframework.data.domain.Pageable pageable) {
        EmpresaId empresaId = EmpresaId.de(tenantProvider.obtenerEmpresaIdActual());
        return productoRepository.listarPorEmpresa(empresaId, pageable)
                .map(this::mapToResponse);
    }

    private ProductoResponse mapToResponse(Producto producto) {
        return new ProductoResponse(
                producto.getProductoId().valor(),
                producto.getEmpresaId().valor(),
                producto.getSku(),
                producto.getNombre(),
                producto.getDescripcion(),
                producto.getCategoriaId().valor(),
                producto.getUnidadMedida().name(),
                producto.getPrecioCompra(),
                producto.getPrecioVenta(),
                producto.getImpuesto().name(),
                producto.getCodigoBarras(),
                producto.getEstado().name(),
                producto.getCreadoEn(),
                producto.getActualizadoEn()
        );
    }
}
