package com.SITFAI_CORE_ERP_TIENDA.fulfillment.infrastructure.adapter.in.messaging;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.event.PedidoConfirmadoEvent;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.application.dto.LineaDespachoCommand;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.application.dto.PlanificarDespachoCommand;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.application.port.input.PlanificarDespachoUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Component("fulfillmentPedidoConfirmadoEventListener")
public class PedidoConfirmadoEventListener {

    private static final Logger log = LoggerFactory.getLogger(PedidoConfirmadoEventListener.class);

    private final PlanificarDespachoUseCase planificarDespachoUseCase;

    public PedidoConfirmadoEventListener(PlanificarDespachoUseCase planificarDespachoUseCase) {
        this.planificarDespachoUseCase = Objects.requireNonNull(planificarDespachoUseCase);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPedidoConfirmado(PedidoConfirmadoEvent event) {
        Objects.requireNonNull(event, "PedidoConfirmadoEvent no puede ser nulo");
        
        log.info("Fulfillment (WMS) intercepta PedidoConfirmadoEvent: pedidoId={}, empresaId={}", 
                event.pedidoId().valor(), event.empresaId().valor());

        UUID empresaId = event.empresaId().valor();
        UUID pedidoId = event.pedidoId().valor();

        java.util.List<com.SITFAI_CORE_ERP_TIENDA.fulfillment.application.dto.LineaDespachoCommand> lineasComando = event.lineas().stream()
                .map(linea -> new com.SITFAI_CORE_ERP_TIENDA.fulfillment.application.dto.LineaDespachoCommand(
                        linea.getProductoId().valor(),
                        linea.getCantidad()
                ))
                .toList();

        PlanificarDespachoCommand planificarCommand = new PlanificarDespachoCommand(
                empresaId,
                pedidoId,
                "Dirección por defecto (Fulfillment Auto)", // direccionLocal
                "Ciudad Base", // ciudad
                "00000", // codigoPostal
                lineasComando
        );
        
        planificarDespachoUseCase.ejecutar(planificarCommand);

        log.info("Auto-Fulfillment completado para Pedido: " + pedidoId);
    }
}
