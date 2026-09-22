package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.in.messaging;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.event.PedidoCreadoEvent;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.ReservarStockCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.port.input.ReservarStockUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class PedidoCreadoEventHandler {

    private static final Logger log = LoggerFactory.getLogger(PedidoCreadoEventHandler.class);
    private final ReservarStockUseCase reservarStockUseCase;

    public PedidoCreadoEventHandler(ReservarStockUseCase reservarStockUseCase) {
        this.reservarStockUseCase = reservarStockUseCase;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(PedidoCreadoEvent event) {
        log.info("Procesando evento PedidoCreadoEvent para pedido {} de empresa {}", event.pedidoId().valor(), event.empresaId().valor());

        for (var item : event.items()) {
            ReservarStockCommand command = new ReservarStockCommand(
                    event.empresaId().valor(),
                    item.getProductoId().valor(),
                    item.getCantidad().aBigDecimal(),
                    event.pedidoId().valor().toString()
            );

            try {
                reservarStockUseCase.ejecutar(command);
                log.info("Stock reservado exitosamente para producto {} en pedido {}", item.getProductoId().valor(), event.pedidoId().valor());
            } catch (Exception e) {
                log.error("Fallo al reservar stock para producto {} en pedido {}", item.getProductoId().valor(), event.pedidoId().valor(), e);
                // Si falla (ej. StockInsuficienteException), el SAGA debería emitir un evento de compensación (ReservaFallidaEvent).
                // Pero por ahora solo logueamos el error y dejamos que aborte esta transacción local de reserva.
            }
        }
    }
}
