package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.in.messaging;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.RegistrarMovimientoCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.port.input.RegistrarMovimientoUseCase;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.event.DevolucionRegistradaEvent;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.valueobject.SucursalId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Event Listener: Intercepta las devoluciones de POS y envía la mercadería
 * a una Bodega de Cuarentena (Regla CAJ-08).
 */
@Component
public class DevolucionPosEventListener {

    private static final Logger log = LoggerFactory.getLogger(DevolucionPosEventListener.class);
    
    private final RegistrarMovimientoUseCase registrarMovimientoUseCase;

    public DevolucionPosEventListener(RegistrarMovimientoUseCase registrarMovimientoUseCase) {
        this.registrarMovimientoUseCase = registrarMovimientoUseCase;
    }

    @Async
    @EventListener
    public void onDevolucionRegistrada(DevolucionRegistradaEvent event) {
        log.info("Inventory: Interceptado DevolucionRegistradaEvent. Evento ID: {}", event.eventoId());

        EmpresaId empresaId = new EmpresaId(event.empresaId().value());
        SucursalId sucursalId = new SucursalId(event.sucursalId().value());
        // Resolución de la Bodega de Cuarentena (Determinista según la sucursal)
        String bodegaCuarentenaId = sucursalId.value().toString() + "-CUARENTENA";

        for (DevolucionRegistradaEvent.LineaDevolucion linea : event.lineas()) {
            RegistrarMovimientoCommand command = new RegistrarMovimientoCommand(
                    empresaId.value().toString(),
                    bodegaCuarentenaId,
                    linea.productoId().toString(),
                    new BigDecimal(linea.cantidad()),
                    "ENTRADA",
                    "DEVOLUCION_POS",
                    event.eventoId().toString() // Documento fuente
            );

            try {
                registrarMovimientoUseCase.ejecutar(command);
                log.info("Stock retornado a CUARENTENA exitosamente. Producto: {}, Cantidad: {}", linea.productoId(), linea.cantidad());
            } catch (Exception e) {
                log.error("Fallo al ingresar devolución a Cuarentena para el producto {}: {}", linea.productoId(), e.getMessage());
            }
        }
    }
}
