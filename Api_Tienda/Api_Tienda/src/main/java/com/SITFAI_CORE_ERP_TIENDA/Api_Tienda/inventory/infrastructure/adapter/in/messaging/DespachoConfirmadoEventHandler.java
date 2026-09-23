package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.in.messaging;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.event.DespachoConfirmadoEvent;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.event.DomainEvent;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.Bodega;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.port.output.BodegaRepository;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.DocumentoFuenteId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * Cierre de Ciclo (El Oyente): Event Handler para {@link DespachoConfirmadoEvent}.
 * <p>
 * Atrapa el evento emitido al confirmar un Despacho, carga la Bodega correspondiente
 * y ejecuta el método de dominio {@code deducirStockReservado} para eliminar definitivamente
 * las unidades físicas del inventario y persistir el cambio en la bodega.
 * <p>
 * Regla 1: Adaptador de infraestructura event-driven.
 * Regla BOD-03, BOD-04, MT-01: Documento fuente "DESPACHO" y aislamiento multitenant.
 */
@Component
public class DespachoConfirmadoEventHandler {

    private static final Logger log = LoggerFactory.getLogger(DespachoConfirmadoEventHandler.class);

    private final BodegaRepository bodegaRepository;
    private final ApplicationEventPublisher eventPublisher;

    public DespachoConfirmadoEventHandler(
            BodegaRepository bodegaRepository,
            ApplicationEventPublisher eventPublisher) {
        this.bodegaRepository = Objects.requireNonNull(bodegaRepository, "BodegaRepository es obligatorio");
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "ApplicationEventPublisher es obligatorio");
    }

    @EventListener
    public void onDespachoConfirmado(DespachoConfirmadoEvent event) {
        Objects.requireNonNull(event, "DespachoConfirmadoEvent no puede ser nulo");

        log.info("Procesando DespachoConfirmadoEvent: despachoId={}, pedidoId={}, empresaId={}",
                event.despachoId().valor(), event.pedidoId().valor(), event.empresaId().valor());

        // 1. Cargar la Bodega correspondiente
        Bodega bodega;
        if (event.bodegaId() != null) {
            bodega = bodegaRepository.buscarPorId(event.bodegaId(), event.empresaId())
                    .orElseThrow(() -> new IllegalStateException(
                            String.format("Bodega '%s' no encontrada para el tenant '%s'",
                                    event.bodegaId().valor(), event.empresaId().valor())));
        } else {
            List<Bodega> bodegas = bodegaRepository.listarActivasPorEmpresa(event.empresaId());
            if (bodegas.isEmpty()) {
                throw new IllegalStateException(
                        String.format("No se encontró ninguna Bodega activa para la Empresa '%s'",
                                event.empresaId().valor()));
            }
            bodega = bodegas.get(0);
        }

        // 2. Ejecutar el método de dominio deducirStockReservado para eliminar las unidades físicas
        DocumentoFuenteId docFuente = new DocumentoFuenteId("DESPACHO", event.despachoId().valor().toString());

        for (DespachoConfirmadoEvent.LineaDespachoDetalle linea : event.lineas()) {
            bodega.deducirStockReservado(linea.productoId(), linea.cantidad(), docFuente);
        }

        // 3. Persistir el cambio en la bodega
        Bodega guardada = bodegaRepository.guardar(bodega);

        // 4. Publicar eventos derivados generados por la bodega
        List<DomainEvent> domainEvents = guardada.drainDomainEvents();
        domainEvents.forEach(eventPublisher::publishEvent);

        log.info("Stock reservado deducido exitosamente en Bodega [{}] tras confirmar despacho [{}]",
                bodega.getId().valor(), event.despachoId().valor());
    }
}
