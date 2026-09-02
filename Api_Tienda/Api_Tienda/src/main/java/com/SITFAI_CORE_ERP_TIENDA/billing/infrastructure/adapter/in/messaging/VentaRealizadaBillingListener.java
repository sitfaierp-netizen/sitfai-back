package com.SITFAI_CORE_ERP_TIENDA.billing.infrastructure.adapter.in.messaging;

import com.SITFAI_CORE_ERP_TIENDA.billing.application.dto.EmitirFacturaCommand;
import com.SITFAI_CORE_ERP_TIENDA.billing.application.port.input.EmitirFacturaUseCase;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.event.VentaRegistradaEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Event Listener: Adaptador de entrada asíncrono para emitir facturas
 * automáticamente cuando se registra una venta POS (Coreografía de eventos).
 */
@Component
public class VentaRealizadaBillingListener {

    private static final Logger log = LoggerFactory.getLogger(VentaRealizadaBillingListener.class);
    public static final String NIT_CONSUMIDOR_FINAL = "222222222222";

    private final EmitirFacturaUseCase emitirFacturaUseCase;

    public VentaRealizadaBillingListener(EmitirFacturaUseCase emitirFacturaUseCase) {
        this.emitirFacturaUseCase = emitirFacturaUseCase;
    }

    @Async
    @EventListener
    public void onVentaRegistrada(VentaRegistradaEvent event) {
        log.info("Billing: Interceptado VentaRegistradaEvent. Evento ID: {}", event.getEventId());

        String nitReceptor = (event.clienteNit() != null && !event.clienteNit().isBlank())
                ? event.clienteNit()
                : NIT_CONSUMIDOR_FINAL;

        // Mapeo de líneas al nuevo contrato LineaFacturaCommand
        List<EmitirFacturaCommand.LineaFacturaCommand> lineas = event.lineas().stream()
                .map(l -> new EmitirFacturaCommand.LineaFacturaCommand(
                        "Item " + l.productoId(),
                        new BigDecimal(l.cantidad()),
                        l.precioUnitario() != null ? l.precioUnitario() : BigDecimal.ZERO,
                        "COP",
                        List.of(new EmitirFacturaCommand.ImpuestoCommand("IVA", new BigDecimal("19")))
                ))
                .collect(Collectors.toList());

        // empresaId viene del evento de venta (MT-01)
        UUID empresaId = event.empresaId() != null ? event.empresaId().value() : null;

        EmitirFacturaCommand command = new EmitirFacturaCommand(
                empresaId,
                null,  // clienteId no disponible en VentaRegistradaEvent
                null,  // pedidoId no aplica en flujo POS
                nitReceptor,
                lineas
        );

        try {
            emitirFacturaUseCase.emitirFactura(command);
            log.info("Factura electrónica orquestada exitosamente.");
        } catch (Exception e) {
            log.error("Falló la orquestación de la factura para el evento {}: {}", event.getEventId(), e.getMessage());
        }
    }
}
