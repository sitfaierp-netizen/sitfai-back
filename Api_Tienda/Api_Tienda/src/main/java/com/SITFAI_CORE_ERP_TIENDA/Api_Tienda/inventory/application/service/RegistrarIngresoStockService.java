package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.service;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.RegistrarIngresoStockCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.port.input.RegistrarIngresoStockUseCase;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.port.output.TenantProviderPort;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.exception.BodegaNoEncontradaException;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.Bodega;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.port.output.BodegaEventPublisher;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.port.output.BodegaRepository;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.BodegaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.Cantidad;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.DocumentoFuenteId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.LoteId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.ProductoId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

@Service
@Transactional
public class RegistrarIngresoStockService implements RegistrarIngresoStockUseCase {

    private final BodegaRepository bodegaRepository;
    private final TenantProviderPort tenantProvider;
    private final BodegaEventPublisher eventPublisher;

    public RegistrarIngresoStockService(BodegaRepository bodegaRepository, TenantProviderPort tenantProvider, BodegaEventPublisher eventPublisher) {
        this.bodegaRepository = Objects.requireNonNull(bodegaRepository);
        this.tenantProvider = Objects.requireNonNull(tenantProvider);
        this.eventPublisher = Objects.requireNonNull(eventPublisher);
    }

    @Override
    public void registrarIngreso(RegistrarIngresoStockCommand command) {
        // 1. Extraer tenant seguro (MT-01)
        EmpresaId empresaId = tenantProvider.getEmpresaIdAutenticada();
        BodegaId bodegaId = new BodegaId(command.bodegaId());
        ProductoId productoId = new ProductoId(command.productoId());
        Cantidad cantidad = Cantidad.de(command.cantidad());
        LoteId loteId = command.loteId() != null && !command.loteId().isBlank() 
                ? LoteId.de(command.loteId()) 
                : LoteId.de(UUID.randomUUID().toString()); // Lote generado si no viene
        DocumentoFuenteId docFuente = new DocumentoFuenteId(command.docFuenteTipo(), command.docFuenteNumero());

        // 2. Buscar Bodega garantizando aislamiento
        Bodega bodega = bodegaRepository.buscarPorId(bodegaId, empresaId)
                .orElseThrow(() -> new BodegaNoEncontradaException(bodegaId, empresaId));

        // 3. Invocar lógica de dominio
        bodega.registrarIngreso(productoId, cantidad, loteId, command.fechaCaducidad(), docFuente);

        // 4. Guardar
        bodegaRepository.guardar(bodega);

        // 5. Publicar Eventos de Dominio
        eventPublisher.publicarTodos(bodega.drainDomainEvents());
    }
}
