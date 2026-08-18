package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.in.messaging;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.RegistrarMovimientoCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.port.input.RegistrarMovimientoUseCase;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.event.VentaRegistradaEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Objects;
import java.util.UUID;

@Component
public class VentaPosEventListener {

    private static final Logger log = LoggerFactory.getLogger(VentaPosEventListener.class);

    private final RegistrarMovimientoUseCase registrarMovimientoUseCase;

    public VentaPosEventListener(RegistrarMovimientoUseCase registrarMovimientoUseCase) {
        this.registrarMovimientoUseCase = Objects.requireNonNull(registrarMovimientoUseCase);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onVentaRegistrada(VentaRegistradaEvent event) {
        Objects.requireNonNull(event, "VentaRegistradaEvent no puede ser nulo");

        log.info("Inventory intercepta VentaRegistradaEvent: empresaId={}, sucursalId={}",
                event.empresaId().value(), event.sucursalId().value());

        // Resolución de Bodega (mockeada/asumida como la bodega principal de la sucursal)
        // En un entorno real, consultaríamos un servicio de dominio para resolver la BodegaId
        // asociada a la SucursalId. Aquí asignamos un UUID determinístico o extraemos de configuración.
        UUID bodegaPrincipalSucursal = event.sucursalId().value(); // Mock: 1 a 1 Sucursal -> Bodega

        for (VentaRegistradaEvent.LineaVenta linea : event.lineas()) {
            RegistrarMovimientoCommand command = new RegistrarMovimientoCommand(
                    event.empresaId().value().toString(),
                    bodegaPrincipalSucursal.toString(),
                    linea.productoId().toString(),
                    java.math.BigDecimal.valueOf(linea.cantidad()),
                    "SALIDA", // Tipo de movimiento
                    "VENTA_POS",
                    event.eventoId().toString()
            );

            registrarMovimientoUseCase.ejecutar(command);
        }

        log.info("Stock descontado exitosamente para la venta del POS.");
    }
}
