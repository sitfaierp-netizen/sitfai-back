package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.in.messaging;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.RegistrarMovimientoCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.port.input.RegistrarMovimientoUseCase;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.event.OrdenCompraRecibidaEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Event Listener: Intercepta las órdenes de compra recibidas y envía la mercadería
 * a la bodega (Putaway) cumpliendo con la Regla BOD-03 y BOD-04.
 */
@Component
public class OrdenCompraRecibidaEventListener {

    private static final Logger log = LoggerFactory.getLogger(OrdenCompraRecibidaEventListener.class);
    
    private final RegistrarMovimientoUseCase registrarMovimientoUseCase;

    public OrdenCompraRecibidaEventListener(RegistrarMovimientoUseCase registrarMovimientoUseCase) {
        this.registrarMovimientoUseCase = registrarMovimientoUseCase;
    }

    @Async
    @EventListener
    public void onOrdenCompraRecibida(OrdenCompraRecibidaEvent event) {
        log.info("Inventory: Interceptado OrdenCompraRecibidaEvent. Evento ID: {}", event.eventoId());

        String empresaIdStr = event.empresaId().valor().toString();
        // Resolución de la Bodega Matriz o Principal del Tenant
        String bodegaMatrizId = empresaIdStr + "-MATRIZ"; 
        String docFuenteNumero = event.ordenCompraId().valor().toString();

        for (OrdenCompraRecibidaEvent.LineaRecibida linea : event.lineas()) {
            RegistrarMovimientoCommand command = new RegistrarMovimientoCommand(
                    empresaIdStr,
                    bodegaMatrizId,
                    linea.productoId().toString(),
                    linea.cantidad(),
                    "ENTRADA",
                    "ORDEN_COMPRA", // Regla BOD-04: Documento Fuente = ORDEN_COMPRA
                    docFuenteNumero // Regla BOD-04: El ID de la Orden de Compra
            );

            try {
                registrarMovimientoUseCase.ejecutar(command);
                log.info("Stock ingresado (Putaway) exitosamente a MATRIZ. Producto: {}, Cantidad: {}", linea.productoId(), linea.cantidad());
            } catch (Exception e) {
                log.error("Fallo al ingresar putaway para el producto {}: {}", linea.productoId(), e.getMessage());
            }
        }
    }
}
