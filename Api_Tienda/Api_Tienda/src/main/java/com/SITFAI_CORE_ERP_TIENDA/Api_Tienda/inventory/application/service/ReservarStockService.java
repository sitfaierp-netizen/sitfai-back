package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.service;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.ReservarStockCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.port.input.ReservarStockUseCase;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.exception.BodegaNoEncontradaException;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.Bodega;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.port.output.BodegaEventPublisher;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.port.output.BodegaRepository;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.EmpresaId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.context.ApplicationEventPublisher;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.exception.StockInsuficienteException;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.event.ReservaRechazadaEvent;

@Service
@Transactional
public class ReservarStockService implements ReservarStockUseCase {

    private final BodegaRepository bodegaRepository;
    private final BodegaEventPublisher eventPublisher;
    private final ApplicationEventPublisher springEventPublisher;

    public ReservarStockService(BodegaRepository bodegaRepository, BodegaEventPublisher eventPublisher, ApplicationEventPublisher springEventPublisher) {
        this.bodegaRepository = Objects.requireNonNull(bodegaRepository);
        this.eventPublisher = Objects.requireNonNull(eventPublisher);
        this.springEventPublisher = Objects.requireNonNull(springEventPublisher);
    }

    @Override
    public void ejecutar(ReservarStockCommand command) {
        EmpresaId empresaId = command.toEmpresaId();

        // Buscamos una bodega de venta para el tenant dado
        List<Bodega> bodegas = bodegaRepository.listarActivasPorEmpresa(empresaId);
        Optional<Bodega> bodegaVentaOpt = bodegas.stream()
                .filter(Bodega::puedeVender)
                .findFirst();

        if (bodegaVentaOpt.isEmpty()) {
            throw new BodegaNoEncontradaException(null, empresaId);
        }

        Bodega bodega = bodegaVentaOpt.get();

        try {
            bodega.reservarStock(command.toProductoId(), command.toCantidad(), command.toDocumentoFuenteId());
            bodegaRepository.guardar(bodega);
            eventPublisher.publicarTodos(bodega.drainDomainEvents());
        } catch (StockInsuficienteException e) {
            // Unhappy Path: Violación de la regla BOD-05
            // Publicamos el evento de compensación SAGA enriquecido con empresaId (MT-01)
            ReservaRechazadaEvent evento = ReservaRechazadaEvent.of(
                    empresaId,
                    java.util.UUID.fromString(command.referenciaOrigen()), // referenciaOrigen lleva el PedidoId
                    command.toProductoId(),
                    "Rechazado por regla BOD-05: " + e.getMessage()
            );
            springEventPublisher.publishEvent(evento);
            // Re-lanzar o no re-lanzar depende de si el backend debe devolver 400. 
            // Como es asíncrono en SAGA, relanzar podría causar rollback del evento de Spring si no se maneja,
            // pero SpringEvents (sin @TransactionalEventListener o con rollback) pueden comportarse distinto.
            // Para asegurar la coreografía y detener el flujo local, re-lanzamos la excepción.
            throw e;
        }
    }
}
