package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.service;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.RecepcionMercanciaResponse;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.RecepcionarMercanciaCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.port.input.RecepcionarMercanciaUseCase;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.port.output.TenantProviderPort;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.event.DomainEvent;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.event.IngresoStockRegistradoEvent;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.exception.BodegaNoEncontradaException;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.Bodega;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.port.output.BodegaEventPublisher;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.port.output.BodegaRepository;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Servicio de Aplicación: Orquestador transaccional para la Recepción Física de Mercancía (Inbound Logistics).
 * <p>
 * Regla 1 (Clean Architecture): Orquesta el dominio, invoca puertos de salida puros.
 * Regla MT-01: Extrae de forma segura el tenant del contexto de seguridad.
 * Regla BOD-04: Todo movimiento de entrada exige un documento fuente traceable (ORDEN_COMPRA).
 */
@Service
public class RecepcionarMercanciaService implements RecepcionarMercanciaUseCase {

    private static final Logger log = LoggerFactory.getLogger(RecepcionarMercanciaService.class);

    private final BodegaRepository bodegaRepository;
    private final TenantProviderPort tenantProviderPort;
    private final BodegaEventPublisher eventPublisher;
    private final ApplicationEventPublisher springEventPublisher;

    public RecepcionarMercanciaService(
            BodegaRepository bodegaRepository,
            TenantProviderPort tenantProviderPort,
            BodegaEventPublisher eventPublisher,
            ApplicationEventPublisher springEventPublisher
    ) {
        this.bodegaRepository = Objects.requireNonNull(bodegaRepository, "BodegaRepository es obligatorio");
        this.tenantProviderPort = Objects.requireNonNull(tenantProviderPort, "TenantProviderPort es obligatorio");
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "BodegaEventPublisher es obligatorio");
        this.springEventPublisher = Objects.requireNonNull(springEventPublisher, "ApplicationEventPublisher es obligatorio");
    }

    @Override
    @Transactional
    public RecepcionMercanciaResponse recepcionar(RecepcionarMercanciaCommand command) {
        Objects.requireNonNull(command, "RecepcionarMercanciaCommand no puede ser nulo");

        // 1. Extraer el empresa_id de forma segura mediante los puertos de seguridad (MT-01)
        EmpresaId empresaId = tenantProviderPort.getEmpresaIdAutenticada();
        BodegaId bodegaId = BodegaId.de(command.bodegaId());

        log.info("Iniciando recepción de mercancía en Bodega [{}] para Empresa [{}] bajo OC [{}]",
                bodegaId.valor(), empresaId.valor(), command.ordenCompraId());

        // 2. Cargar la Bodega a través de su repositorio exigiendo el EmpresaId (MT-01)
        Bodega bodega = bodegaRepository.buscarPorId(bodegaId, empresaId)
                .orElseThrow(() -> new BodegaNoEncontradaException(bodegaId, empresaId));

        // 3. Registrar el ingreso referenciando el DocumentoFuenteId (tipo ORDEN_COMPRA) (BOD-04)
        DocumentoFuenteId docFuente = new DocumentoFuenteId("ORDEN_COMPRA", command.ordenCompraId().toString());

        for (RecepcionarMercanciaCommand.LoteRecepcionCommand loteCmd : command.lotes()) {
            ProductoId productoId = new ProductoId(loteCmd.productoId());
            Cantidad cantidad = Cantidad.de(loteCmd.cantidad());
            LoteId loteId = (loteCmd.codigoLote() != null && !loteCmd.codigoLote().isBlank())
                    ? LoteId.de(loteCmd.codigoLote().trim())
                    : LoteId.de("LOTE-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());

            bodega.registrarIngreso(productoId, cantidad, loteId, loteCmd.fechaCaducidad(), docFuente);
        }

        // 4. Guardar la bodega con su stock y movimientos actualizados
        Bodega guardada = bodegaRepository.guardar(bodega);

        // 5. Publicar los eventos resultantes (Domain Events acumulados + IngresoStockRegistradoEvent)
        List<DomainEvent> domainEvents = guardada.drainDomainEvents();
        eventPublisher.publicarTodos(domainEvents);
        domainEvents.forEach(springEventPublisher::publishEvent);

        IngresoStockRegistradoEvent ingresoEvent = IngresoStockRegistradoEvent.of(
                empresaId,
                bodegaId,
                docFuente,
                command.lotes().size()
        );
        eventPublisher.publicar(ingresoEvent);
        springEventPublisher.publishEvent(ingresoEvent);

        log.info("Recepción de mercancía completada exitosamente en Bodega [{}]. [{}] lotes ingresados.",
                bodegaId.valor(), command.lotes().size());

        return new RecepcionMercanciaResponse(
                command.bodegaId(),
                command.ordenCompraId(),
                command.lotes().size(),
                "Mercancía recepcionada exitosamente en bodega"
        );
    }
}
