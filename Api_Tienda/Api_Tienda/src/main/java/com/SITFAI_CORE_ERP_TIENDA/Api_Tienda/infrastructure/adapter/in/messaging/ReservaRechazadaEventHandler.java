package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.infrastructure.adapter.in.messaging;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.dto.CancelarPedidoCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.port.input.CancelarPedidoUseCase;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.event.ReservaRechazadaEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.UUID;

/**
 * Adaptador de Entrada (Driving Adapter) para la coreografía SAGA asíncrona.
 * <p>
 * Escucha el evento {@link ReservaRechazadaEvent} publicado por el Bounded Context de Inventario
 * y ejecuta la compensación cancelando el Pedido en E-Commerce vía {@link CancelarPedidoUseCase}.
 * <p>
 * Reglas validadas: REGLA-1 (Adaptador de Entrada), REGLA-3 (Eventos de Integración), BOD-05 (Compensación SAGA).
 */
@Component
public class ReservaRechazadaEventHandler {

    private static final Logger log = LoggerFactory.getLogger(ReservaRechazadaEventHandler.class);
    private static final UUID EMPRESA_SISTEMA_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    private final CancelarPedidoUseCase cancelarPedidoUseCase;

    public ReservaRechazadaEventHandler(CancelarPedidoUseCase cancelarPedidoUseCase) {
        this.cancelarPedidoUseCase = Objects.requireNonNull(cancelarPedidoUseCase);
    }

    /**
     * Maneja el evento de rechazo de reserva. La ejecución es asíncrona (@Async)
     * para no bloquear el hilo de inventario que publicó el evento.
     */
    @Async
    @EventListener
    public void onReservaRechazada(ReservaRechazadaEvent evento) {
        log.warn("[SAGA-COMPENSACION] Reserva rechazada para pedidoId={} | productoId={} | motivo='{}'",
                evento.pedidoId(), evento.productoId().valor(), evento.motivo());

        try {
            // El pedidoId fue mapeado como DocumentoFuenteId durante la creación del evento.
            // empresaId: Para la compensación, el sistema usa el ID del pedido y asume el tenant del evento.
            // En una implementación robusta, el ReservaRechazadaEvent debería incluir el empresaId.
            // Aquí usamos un empresaId de sistema para el lookup, que en una implementación real
            // debería estar incluido en el payload del evento de integración.
            CancelarPedidoCommand command = new CancelarPedidoCommand(
                    EMPRESA_SISTEMA_UUID,
                    evento.pedidoId(),
                    "SAGA-COMPENSACION: " + evento.motivo()
            );

            cancelarPedidoUseCase.ejecutar(command);
            log.info("[SAGA-COMPENSACION] Pedido {} cancelado exitosamente por compensación.", evento.pedidoId());
        } catch (Exception e) {
            log.error("[SAGA-COMPENSACION] Error al cancelar pedido {} por compensación SAGA: {}",
                    evento.pedidoId(), e.getMessage(), e);
        }
    }
}
