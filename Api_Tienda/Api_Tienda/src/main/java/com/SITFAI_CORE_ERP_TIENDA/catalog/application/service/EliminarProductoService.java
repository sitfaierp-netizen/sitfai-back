package com.SITFAI_CORE_ERP_TIENDA.catalog.application.service;

import com.SITFAI_CORE_ERP_TIENDA.catalog.application.port.input.EliminarProductoUseCase;
import com.SITFAI_CORE_ERP_TIENDA.catalog.application.port.output.ProductoRepository;
import com.SITFAI_CORE_ERP_TIENDA.catalog.application.port.output.TenantProviderPort;
import com.SITFAI_CORE_ERP_TIENDA.catalog.domain.exception.DomainException;
import com.SITFAI_CORE_ERP_TIENDA.catalog.domain.exception.ProductoNoEncontradoException;
import com.SITFAI_CORE_ERP_TIENDA.catalog.domain.model.Producto;
import com.SITFAI_CORE_ERP_TIENDA.catalog.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.catalog.domain.valueobject.ProductoId;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

@Service
@Transactional
public class EliminarProductoService implements EliminarProductoUseCase {

    private final TenantProviderPort tenantProvider;
    private final ProductoRepository productoRepository;

    public EliminarProductoService(TenantProviderPort tenantProvider,
                                   ProductoRepository productoRepository) {
        this.tenantProvider = Objects.requireNonNull(tenantProvider);
        this.productoRepository = Objects.requireNonNull(productoRepository);
    }

    @Override
    public void eliminar(UUID productoId) {
        EmpresaId empresaId = EmpresaId.de(tenantProvider.obtenerEmpresaIdActual());
        ProductoId id = new ProductoId(productoId);

        Producto producto = productoRepository
                .buscarPorIdYEmpresa(id, empresaId)
                .orElseThrow(() -> new ProductoNoEncontradoException(productoId.toString()));

        try {
            productoRepository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw new DomainException("No se puede eliminar el producto porque ya tiene movimientos de inventario o transacciones asociadas. Considere cambiar su estado a INACTIVO.");
        }
    }
}
