package com.SITFAI_CORE_ERP_TIENDA.billing.infrastructure.adapter.in.event;

import com.SITFAI_CORE_ERP_TIENDA.billing.application.dto.EmitirFacturaCommand;
import com.SITFAI_CORE_ERP_TIENDA.billing.application.dto.FacturaResponse;
import com.SITFAI_CORE_ERP_TIENDA.billing.application.port.input.EmitirFacturaUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * Adaptador de Entrada Asincrónico: Listener de Confirmación de Pedidos.
 * Emite facturas automáticamente cuando se confirma un pedido.
 */
@Component("billingPedidoConfirmadoEventListener")
public class PedidoConfirmadoEventListener {

    private static final Logger log = LoggerFactory.getLogger(PedidoConfirmadoEventListener.class);

    private final EmitirFacturaUseCase emitirFacturaUseCase;

    public PedidoConfirmadoEventListener(EmitirFacturaUseCase emitirFacturaUseCase) {
        this.emitirFacturaUseCase = Objects.requireNonNull(emitirFacturaUseCase);
    }

    // Nota: El tipo del evento está comentado porque PedidoConfirmadoEvent pertenece a otro BC.
    // En producción se usará un contrato de evento compartido o Kafka.
    // @EventListener
    public void onPedidoConfirmado(Object event) {
        log.info("PedidoConfirmadoEventListener: procesando evento de confirmación de pedido");
        // Implementación pendiente de integración cross-BC
    }
}
