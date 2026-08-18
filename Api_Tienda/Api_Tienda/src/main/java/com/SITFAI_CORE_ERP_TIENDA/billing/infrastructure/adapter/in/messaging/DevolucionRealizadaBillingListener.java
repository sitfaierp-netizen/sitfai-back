package com.SITFAI_CORE_ERP_TIENDA.billing.infrastructure.adapter.in.messaging;

import com.SITFAI_CORE_ERP_TIENDA.billing.application.dto.EmitirNotaCreditoCommand;
import com.SITFAI_CORE_ERP_TIENDA.billing.application.port.input.EmitirNotaCreditoUseCase;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.event.DevolucionRegistradaEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Event Listener: Adaptador de entrada asíncrono para emitir Notas de Crédito
 * automáticamente cuando se registra una devolución (Logística Inversa).
 * 
 * Cumple con Invariante Tributaria DIAN: Extrae facturaId original.
 */
@Component
public class DevolucionRealizadaBillingListener {

    private static final Logger log = LoggerFactory.getLogger(DevolucionRealizadaBillingListener.class);
    
    // Motivo por defecto de devolución en tienda
    private static final String MOTIVO_CODIGO = "02"; 
    private static final String MOTIVO_DESC = "Anulación de factura electrónica por devolución de mercancía";

    private final EmitirNotaCreditoUseCase emitirNotaCreditoUseCase;

    public DevolucionRealizadaBillingListener(EmitirNotaCreditoUseCase emitirNotaCreditoUseCase) {
        this.emitirNotaCreditoUseCase = emitirNotaCreditoUseCase;
    }

    @Async
    @EventListener
    public void onDevolucionRegistrada(DevolucionRegistradaEvent event) {
        log.info("Billing: Interceptado DevolucionRegistradaEvent para emitir Nota de Crédito. Evento ID: {}", event.eventoId());

        if (event.ventaOrigenId() == null) {
            log.error("Violación de Invariante DIAN: El evento de devolución {} carece de ventaOrigenId (factura original). Se cancela la emisión.", event.eventoId());
            return;
        }

        // Mapeo de Líneas a Reversar
        List<EmitirNotaCreditoCommand.LineaReversoDto> lineas = event.lineas().stream()
                .map(l -> new EmitirNotaCreditoCommand.LineaReversoDto(
                        "Devolución Item " + l.productoId(), // Concepto deducido
                        new BigDecimal(l.cantidad()),
                        l.precioUnitario() != null ? l.precioUnitario() : BigDecimal.ZERO,
                        "COP", // Moneda local
                        List.of(new EmitirNotaCreditoCommand.ImpuestoReversoDto("IVA", new BigDecimal("19"))) // Asumiendo IVA 19%
                ))
                .collect(Collectors.toList());

        // Crear Comando (Aislamiento MT-01 aplicado)
        EmitirNotaCreditoCommand command = new EmitirNotaCreditoCommand(
                event.empresaId().value(),
                event.ventaOrigenId(),
                MOTIVO_CODIGO,
                MOTIVO_DESC,
                lineas
        );

        // Ejecución
        try {
            emitirNotaCreditoUseCase.emitirNotaCredito(command);
            log.info("Nota de Crédito orquestada exitosamente para la factura original {}.", event.ventaOrigenId());
        } catch (Exception e) {
            log.error("Falló la orquestación de la Nota de Crédito para el evento {}: {}", event.eventoId(), e.getMessage());
        }
    }
}
