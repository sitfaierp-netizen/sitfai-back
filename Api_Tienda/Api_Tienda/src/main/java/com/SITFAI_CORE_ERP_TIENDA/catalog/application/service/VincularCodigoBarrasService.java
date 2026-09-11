package com.SITFAI_CORE_ERP_TIENDA.catalog.application.service;

import com.SITFAI_CORE_ERP_TIENDA.catalog.application.dto.ProductoResponse;
import com.SITFAI_CORE_ERP_TIENDA.catalog.application.port.input.VincularCodigoBarrasUseCase;
import com.SITFAI_CORE_ERP_TIENDA.catalog.application.port.output.ProductoRepository;
import com.SITFAI_CORE_ERP_TIENDA.catalog.application.port.output.TenantProviderPort;
import com.SITFAI_CORE_ERP_TIENDA.catalog.domain.exception.DomainException;
import com.SITFAI_CORE_ERP_TIENDA.catalog.domain.exception.ProductoNoEncontradoException;
import com.SITFAI_CORE_ERP_TIENDA.catalog.domain.model.Producto;
import com.SITFAI_CORE_ERP_TIENDA.catalog.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.catalog.domain.valueobject.ProductoId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

/**
 * Application Service: Orquesta la vinculación de un código de barras a un Producto.
 */
@Service
@Transactional
public class VincularCodigoBarrasService implements VincularCodigoBarrasUseCase {

    private final TenantProviderPort tenantProvider;
    private final ProductoRepository productoRepository;

    public VincularCodigoBarrasService(TenantProviderPort tenantProvider,
                                       ProductoRepository productoRepository) {
        this.tenantProvider = Objects.requireNonNull(tenantProvider);
        this.productoRepository = Objects.requireNonNull(productoRepository);
    }

    @Override
    public ProductoResponse vincularCodigoBarras(UUID productoId, String codigoBarras) {
        EmpresaId empresaId = EmpresaId.de(tenantProvider.obtenerEmpresaIdActual());
        ProductoId id = new ProductoId(productoId);

        // Validar unicidad del código de barras
        if (codigoBarras != null && !codigoBarras.isBlank()) {
            boolean existe = productoRepository.existeCodigoBarras(codigoBarras, empresaId, id);
            if (existe) {
                throw new DomainException("El código de barras '" + codigoBarras + "' ya pertenece a otro producto activo.");
            }
        }

        Producto producto = productoRepository
                .buscarPorIdYEmpresa(id, empresaId)
                .orElseThrow(() -> new ProductoNoEncontradoException(productoId.toString()));

        producto.vincularCodigoBarras(codigoBarras);

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
