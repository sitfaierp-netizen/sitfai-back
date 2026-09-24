package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.service;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.DescontarStockVentaCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.port.input.DescontarStockVentaUseCase;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.port.output.TenantProviderPort;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.exception.BodegaNoEncontradaException;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.Bodega;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.port.output.BodegaEventPublisher;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.port.output.BodegaRepository;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.BodegaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.Cantidad;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.DocumentoFuenteId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.ProductoId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@Transactional
public class DescontarStockVentaService implements DescontarStockVentaUseCase {

    private final BodegaRepository bodegaRepository;
    private final TenantProviderPort tenantProvider;
    private final BodegaEventPublisher eventPublisher;

    public DescontarStockVentaService(BodegaRepository bodegaRepository, TenantProviderPort tenantProvider, BodegaEventPublisher eventPublisher) {
        this.bodegaRepository = Objects.requireNonNull(bodegaRepository);
        this.tenantProvider = Objects.requireNonNull(tenantProvider);
        this.eventPublisher = Objects.requireNonNull(eventPublisher);
    }

    @Override
    public void ejecutar(DescontarStockVentaCommand command) {
        // 1. Extraer tenant seguro (MT-01)
        EmpresaId empresaId;
        try {
            empresaId = tenantProvider.getEmpresaIdAutenticada();
        } catch (Exception e) {
            // Fallback en caso de que este UseCase sea invocado desde un evento asíncrono
            // donde el contexto de seguridad Spring (ThreadLocal) no esté poblado.
            throw new IllegalStateException("No se pudo extraer el EmpresaId de los puertos de seguridad", e);
        }

        BodegaId bodegaId = new BodegaId(java.util.UUID.fromString(command.bodegaId()));
        ProductoId productoId = new ProductoId(java.util.UUID.fromString(command.productoId()));
        Cantidad cantidad = Cantidad.de(command.cantidad());
        DocumentoFuenteId docFuente = new DocumentoFuenteId("VENTA_POS", command.documentoFuenteId());

        // 2. Buscar Bodega garantizando aislamiento
        Bodega bodega = bodegaRepository.buscarPorId(bodegaId, empresaId)
                .orElseThrow(() -> new BodegaNoEncontradaException(bodegaId, empresaId));

        // 3. Invocar lógica de dominio FEFO
        bodega.descontarStockPorVenta(productoId, cantidad, docFuente);

        // 4. Guardar
        bodegaRepository.guardar(bodega);

        // 5. Publicar Eventos de Dominio
        eventPublisher.publicarTodos(bodega.drainDomainEvents());
    }
}
