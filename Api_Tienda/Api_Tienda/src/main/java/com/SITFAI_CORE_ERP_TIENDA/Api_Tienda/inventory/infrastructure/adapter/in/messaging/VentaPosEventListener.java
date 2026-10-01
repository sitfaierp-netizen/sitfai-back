package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.in.messaging;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.DescontarStockVentaCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.port.input.DescontarStockVentaUseCase;
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

    private final DescontarStockVentaUseCase descontarStockVentaUseCase;

    public VentaPosEventListener(DescontarStockVentaUseCase descontarStockVentaUseCase) {
        this.descontarStockVentaUseCase = Objects.requireNonNull(descontarStockVentaUseCase);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onVentaRegistrada(VentaRegistradaEvent event) {
        Objects.requireNonNull(event, "VentaRegistradaEvent no puede ser nulo");

        log.info("Inventory intercepta VentaRegistradaEvent: empresaId={}, cajaId={}",
                event.empresaId().value(), event.cajaId());

        // Resolución de Bodega (mockeada/asumida como la bodega principal de la sucursal)
        // En un entorno real, consultaríamos un servicio de dominio para resolver la BodegaId
        // asociada a la SucursalId. Aquí asignamos un UUID determinístico o extraemos de configuración.
        UUID bodegaPrincipalSucursal = event.cajaId(); // Mock: 1 a 1 Sucursal -> Bodega

        for (VentaRegistradaEvent.LineaVenta linea : event.lineas()) {
            if (linea.productoId() == null) {
                log.warn("Línea de venta ignorada por productoId nulo. Transacción={}", event.eventoId());
                continue;
            }

            DescontarStockVentaCommand command = new DescontarStockVentaCommand(
                    bodegaPrincipalSucursal.toString(),
                    linea.productoId().toString(),
                    java.math.BigDecimal.valueOf(linea.cantidad()),
                    event.documentoFuenteId()
            );

            descontarStockVentaUseCase.ejecutar(command);
        }

        log.info("Stock descontado exitosamente para la venta del POS.");
    }
}
