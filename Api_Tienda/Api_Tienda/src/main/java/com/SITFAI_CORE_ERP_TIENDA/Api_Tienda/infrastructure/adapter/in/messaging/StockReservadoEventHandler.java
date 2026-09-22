package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.infrastructure.adapter.in.messaging;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.dto.ConfirmarPedidoCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.port.input.ConfirmarPedidoUseCase;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.event.StockReservadoEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.UUID;

/**
 * Adaptador de Entrada (Driving Adapter) para la coreografía SAGA asíncrona (Happy Path).
 * <p>
 * Escucha el evento {@link StockReservadoEvent} publicado por el Bounded Context de Inventario
 * y ejecuta la confirmación del Pedido en E-Commerce vía {@link ConfirmarPedidoUseCase}.
 * <p>
 * Reglas validadas: REGLA-1 (Adaptador de Entrada), REGLA-3 (Eventos de Integración), MT-01 (Aislamiento Multitenant sin HTTP context).
 */
@Component
public class StockReservadoEventHandler {

    private static final Logger log = LoggerFactory.getLogger(StockReservadoEventHandler.class);

    private final ConfirmarPedidoUseCase confirmarPedidoUseCase;

    public StockReservadoEventHandler(ConfirmarPedidoUseCase confirmarPedidoUseCase) {
        this.confirmarPedidoUseCase = Objects.requireNonNull(confirmarPedidoUseCase, "confirmarPedidoUseCase es obligatorio");
    }

    /**
     * Maneja el evento de reserva exitosa de stock. La ejecución es asíncrona (@Async)
     * para desacoplar temporalmente el Bounded Context de Inventario del de E-commerce.
     */
    @Async
    @EventListener
    public void onStockReservado(StockReservadoEvent evento) {
        // Validación del documento fuente — solo reaccionamos a pedidos originados en E-commerce
        if (evento.documentoFuente() == null || !"PEDIDO_ECOMMERCE".equalsIgnoreCase(evento.documentoFuente().tipo())) {
            return;
        }

        UUID pedidoId;
        try {
            pedidoId = UUID.fromString(evento.documentoFuente().numero());
        } catch (IllegalArgumentException e) {
            log.warn("[SAGA-HAPPY-PATH] Documento fuente no contiene un UUID de pedido válido: {}", evento.documentoFuente());
            return;
        }

        log.info("[SAGA-HAPPY-PATH] Stock reservado para tenant={} | pedidoId={} | productoId={} | cantidad={}",
                evento.empresaId().valor(), pedidoId, evento.productoId().valor(), evento.cantidadReservada().valor());

        try {
            // MT-01: El empresaId se extrae directamente del payload del evento (cero dependencia de SecurityContext/HTTP)
            ConfirmarPedidoCommand command = new ConfirmarPedidoCommand(
                    evento.empresaId().valor(),
                    pedidoId
            );

            confirmarPedidoUseCase.ejecutar(command);
            log.info("[SAGA-HAPPY-PATH] Pedido {} del tenant {} confirmado exitosamente tras reserva de stock.",
                    pedidoId, evento.empresaId().valor());
        } catch (Exception e) {
            log.error("[SAGA-HAPPY-PATH] Error al confirmar pedido {} del tenant {} tras reserva de stock: {}",
                    pedidoId, evento.empresaId().valor(), e.getMessage(), e);
        }
    }
}
