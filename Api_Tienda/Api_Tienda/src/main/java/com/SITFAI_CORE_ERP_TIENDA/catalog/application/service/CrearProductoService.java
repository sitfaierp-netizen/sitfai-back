package com.SITFAI_CORE_ERP_TIENDA.catalog.application.service;

import com.SITFAI_CORE_ERP_TIENDA.catalog.application.dto.CrearProductoCommand;
import com.SITFAI_CORE_ERP_TIENDA.catalog.application.dto.ProductoResponse;
import com.SITFAI_CORE_ERP_TIENDA.catalog.application.port.input.CrearProductoUseCase;
import com.SITFAI_CORE_ERP_TIENDA.catalog.application.port.output.CatalogEventPublisherPort;
import com.SITFAI_CORE_ERP_TIENDA.catalog.application.port.output.ProductoRepository;
import com.SITFAI_CORE_ERP_TIENDA.catalog.application.port.output.TenantProviderPort;
import com.SITFAI_CORE_ERP_TIENDA.catalog.domain.model.Producto;
import com.SITFAI_CORE_ERP_TIENDA.catalog.domain.valueobject.CategoriaId;
import com.SITFAI_CORE_ERP_TIENDA.catalog.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.catalog.domain.valueobject.Impuesto;
import com.SITFAI_CORE_ERP_TIENDA.catalog.domain.valueobject.ProductoId;
import com.SITFAI_CORE_ERP_TIENDA.catalog.domain.valueobject.UnidadMedida;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * Application Service: Orquesta la creación de un Producto en el Catálogo.
 *
 * Responsabilidades:
 * 1. Resolver el tenant activo vía {@link TenantProviderPort} (MT-01, MT-06).
 * 2. Construir el Aggregate Root {@link Producto} via su factory method.
 * 3. Persistir el Producto.
 * 4. Drenar y publicar el {@link com.SITFAI_CORE_ERP_TIENDA.catalog.domain.event.ProductoCreadoEvent}.
 * 5. Mapear a DTO de respuesta.
 *
 * NO contiene lógica de negocio — la delega al Agregado (Regla 1).
 */
@Service
@Transactional
public class CrearProductoService implements CrearProductoUseCase {

    private final TenantProviderPort    tenantProvider;
    private final ProductoRepository    productoRepository;
    private final CatalogEventPublisherPort eventPublisher;

    public CrearProductoService(TenantProviderPort tenantProvider,
                                 ProductoRepository productoRepository,
                                 CatalogEventPublisherPort eventPublisher) {
        this.tenantProvider    = Objects.requireNonNull(tenantProvider);
        this.productoRepository = Objects.requireNonNull(productoRepository);
        this.eventPublisher    = Objects.requireNonNull(eventPublisher);
    }

    @Override
    public ProductoResponse crear(CrearProductoCommand command) {
        // 1. Resolver EmpresaId desde contexto de seguridad — NUNCA del command (MT-01)
        EmpresaId empresaId = EmpresaId.de(tenantProvider.obtenerEmpresaIdActual());

        // 2. Crear el Aggregate Root (las invariantes se validan fail-fast aquí)
        Producto producto = Producto.crear(
                ProductoId.generar(),
                empresaId,
                command.sku(),
                command.nombre(),
                command.descripcion(),
                new CategoriaId(command.categoriaId()),
                UnidadMedida.valueOf(command.unidadMedida()),
                command.precioCompra(),
                command.precioVenta(),
                Impuesto.valueOf(command.impuesto()),
                command.codigoBarras()
        );

        // 3. Persistir (el JPA Adapter puede lanzar SkuDuplicadoException)
        productoRepository.guardar(producto);

        // 4. Drenar y publicar Domain Events (post-commit es responsabilidad del publisher)
        producto.drenaEventos().forEach(eventPublisher::publicar);

        // 5. Mapear a DTO
        return toResponse(producto);
    }

    private ProductoResponse toResponse(Producto p) {
        return new ProductoResponse(
                p.getProductoId().valor(),
                p.getEmpresaId().valor(),
                p.getSku(),
                p.getNombre(),
                p.getDescripcion(),
                p.getCategoriaId().valor(),
                p.getUnidadMedida().name(),
                p.getPrecioCompra(),
                p.getPrecioVenta(),
                p.getImpuesto().name(),
                p.getCodigoBarras(),
                p.getEstado().name(),
                p.getCreadoEn(),
                p.getActualizadoEn()
        );
    }
}
