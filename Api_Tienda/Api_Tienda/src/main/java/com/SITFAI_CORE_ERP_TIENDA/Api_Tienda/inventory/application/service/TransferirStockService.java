package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.service;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.TransferirStockCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.exception.BodegaNoEncontradaException;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.Bodega;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.port.input.TransferirStockUseCase;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.port.output.BodegaEventPublisher;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.port.output.BodegaRepository;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.service.TransferenciaStockDomainService;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.BodegaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.Cantidad;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.ProductoId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ═══════════════════════════════════════════════════════════════════════════════
 * APPLICATION SERVICE: Transferir Stock entre Bodegas
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * Orquesta la lógica de aplicación para la transferencia de stock, delegando
 * el core de negocio al Domain Service `TransferenciaStockDomainService`.
 * <p>
 * Reglas validadas a nivel de orquestación: MT-01 (Aislamiento de Inquilinos).
 */
@Service
public class TransferirStockService implements TransferirStockUseCase {

    private final BodegaRepository bodegaRepository;
    private final BodegaEventPublisher eventPublisher;
    
    // Instanciado directamente al ser un servicio de dominio puro y sin estado (REGLA-1)
    private final TransferenciaStockDomainService transferenciaDomainService = new TransferenciaStockDomainService();

    public TransferirStockService(BodegaRepository bodegaRepository, BodegaEventPublisher eventPublisher) {
        this.bodegaRepository = bodegaRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public void ejecutar(TransferirStockCommand command) {
        // 1. Construir Value Objects
        EmpresaId empresaId = EmpresaId.de(command.empresaId());
        BodegaId origenId = BodegaId.de(command.bodegaOrigenId());
        BodegaId destinoId = BodegaId.de(command.bodegaDestinoId());
        ProductoId productoId = ProductoId.de(command.productoId());
        Cantidad cantidad = Cantidad.de(command.cantidad());
        String docFuente = command.documentoFuente();

        // 2. Recuperar Bodega Origen y validar Tenant (MT-01)
        Bodega origen = bodegaRepository.buscarPorId(origenId, empresaId)
                .orElseThrow(() -> new BodegaNoEncontradaException(origenId, empresaId));
        
        // 3. Recuperar Bodega Destino y validar Tenant (MT-01)
        Bodega destino = bodegaRepository.buscarPorId(destinoId, empresaId)
                .orElseThrow(() -> new BodegaNoEncontradaException(destinoId, empresaId));

        // 4. Delegar al Domain Service
        transferenciaDomainService.transferir(origen, destino, productoId, cantidad, docFuente);

        // 5. Persistir los agregados mutados
        bodegaRepository.guardar(origen);
        bodegaRepository.guardar(destino);

        // 6. Publicar eventos de dominio
        eventPublisher.publicarTodos(origen.drainDomainEvents());
        eventPublisher.publicarTodos(destino.drainDomainEvents());
    }
}
