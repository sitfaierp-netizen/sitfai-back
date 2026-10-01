package com.SITFAI_CORE_ERP_TIENDA.purchasing.infrastructure.adapter.in.messaging;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.event.PuntoReordenAlcanzadoEvent;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.dto.ProcesarPuntoReordenCommand;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.port.input.ProcesarPuntoReordenUseCase;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.logging.Logger;

@Component
public class PuntoReordenEventListener {

    private static final Logger LOGGER = Logger.getLogger(PuntoReordenEventListener.class.getName());
    
    private final ProcesarPuntoReordenUseCase procesarPuntoReordenUseCase;

    public PuntoReordenEventListener(ProcesarPuntoReordenUseCase procesarPuntoReordenUseCase) {
        this.procesarPuntoReordenUseCase = procesarPuntoReordenUseCase;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePuntoReordenAlcanzadoEvent(PuntoReordenAlcanzadoEvent event) {
        LOGGER.info("Iniciando auto-replenishment para Producto: " + event.productoId().valor());

        var orden = procesarPuntoReordenUseCase.procesar(new ProcesarPuntoReordenCommand(
                event.empresaId().valor(),
                event.bodegaId().valor(),
                event.productoId().valor(),
                event.cantidadActual()
        ));

        LOGGER.info("Auto-replenishment completado. Creada OC: " + orden.id());
    }
}
