package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.service;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.DescontarStockCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.port.input.DescontarStockUseCase;
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
public class DescontarStockService implements DescontarStockUseCase {

    private final BodegaRepository bodegaRepository;
    private final TenantProviderPort tenantProvider;
    private final BodegaEventPublisher eventPublisher;

    public DescontarStockService(BodegaRepository bodegaRepository, TenantProviderPort tenantProvider, BodegaEventPublisher eventPublisher) {
        this.bodegaRepository = Objects.requireNonNull(bodegaRepository);
        this.tenantProvider = Objects.requireNonNull(tenantProvider);
        this.eventPublisher = Objects.requireNonNull(eventPublisher);
    }

    @Override
    public void descontarStock(DescontarStockCommand command) {
        // 1. Extraer tenant seguro (MT-01)
        EmpresaId empresaId = command.empresaId() != null
                ? new EmpresaId(command.empresaId())
                : tenantProvider.getEmpresaIdAutenticada();
        BodegaId bodegaId = new BodegaId(command.bodegaId());
        ProductoId productoId = new ProductoId(command.productoId());
        Cantidad cantidad = Cantidad.de(command.cantidad());
        DocumentoFuenteId docFuente = new DocumentoFuenteId(command.docFuenteTipo(), command.docFuenteNumero());

        // 2. Buscar Bodega garantizando aislamiento
        Bodega bodega = bodegaRepository.buscarPorId(bodegaId, empresaId)
                .orElseThrow(() -> new BodegaNoEncontradaException(bodegaId, empresaId));

        // 3. Invocar lógica de dominio (FEFO automático, validación BOD-05 y BOD-08)
        bodega.descontarStock(productoId, cantidad, docFuente);

        // 4. Guardar
        bodegaRepository.guardar(bodega);

        // 5. Publicar Eventos de Dominio
        eventPublisher.publicarTodos(bodega.drainDomainEvents());
    }
}
