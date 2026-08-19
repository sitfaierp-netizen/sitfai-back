package com.SITFAI_CORE_ERP_TIENDA.catalog.application.service;

import com.SITFAI_CORE_ERP_TIENDA.catalog.application.dto.ProductoResponse;
import com.SITFAI_CORE_ERP_TIENDA.catalog.application.port.input.CambiarEstadoProductoUseCase;
import com.SITFAI_CORE_ERP_TIENDA.catalog.application.port.output.ProductoRepository;
import com.SITFAI_CORE_ERP_TIENDA.catalog.application.port.output.TenantProviderPort;
import com.SITFAI_CORE_ERP_TIENDA.catalog.domain.exception.ProductoNoEncontradoException;
import com.SITFAI_CORE_ERP_TIENDA.catalog.domain.model.Producto;
import com.SITFAI_CORE_ERP_TIENDA.catalog.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.catalog.domain.valueobject.EstadoProducto;
import com.SITFAI_CORE_ERP_TIENDA.catalog.domain.valueobject.ProductoId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

/**
 * Application Service: Orquesta el cambio de estado de un Producto.
 * La invariante [INV-CAT-05] (DESCONTINUADO→ACTIVO) se valida en el Agregado.
 */
@Service
@Transactional
public class CambiarEstadoProductoService implements CambiarEstadoProductoUseCase {

    private final TenantProviderPort tenantProvider;
    private final ProductoRepository productoRepository;

    public CambiarEstadoProductoService(TenantProviderPort tenantProvider,
                                         ProductoRepository productoRepository) {
        this.tenantProvider    = Objects.requireNonNull(tenantProvider);
        this.productoRepository = Objects.requireNonNull(productoRepository);
    }

    @Override
    public ProductoResponse cambiarEstado(UUID productoId, String nuevoEstado) {
        // 1. Resolver tenant (MT-01)
        EmpresaId empresaId = EmpresaId.de(tenantProvider.obtenerEmpresaIdActual());

        // 2. Cargar Agregado filtrado por tenant
        Producto producto = productoRepository
                .buscarPorIdYEmpresa(new ProductoId(productoId), empresaId)
                .orElseThrow(() -> new ProductoNoEncontradoException(productoId.toString()));

        // 3. Delegar transición al Dominio (lanza DomainException si viola INV-CAT-05)
        producto.cambiarEstado(EstadoProducto.valueOf(nuevoEstado));

        // 4. Persistir
        productoRepository.guardar(producto);

        return toResponse(producto);
    }

    private ProductoResponse toResponse(Producto p) {
        return new ProductoResponse(
                p.getProductoId().valor(), p.getEmpresaId().valor(),
                p.getSku(), p.getNombre(), p.getDescripcion(),
                p.getCategoriaId().valor(), p.getUnidadMedida().name(),
                p.getPrecioCompra(), p.getPrecioVenta(), p.getImpuesto().name(),
                p.getCodigoBarras(), p.getEstado().name(),
                p.getCreadoEn(), p.getActualizadoEn()
        );
    }
}
