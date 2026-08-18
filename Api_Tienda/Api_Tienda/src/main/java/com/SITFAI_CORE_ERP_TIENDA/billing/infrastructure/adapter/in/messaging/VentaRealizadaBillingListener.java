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
import java.util.stream.Collectors;

/**
 * Event Listener: Adaptador de entrada asíncrono para emitir facturas
 * automáticamente cuando se registra una venta (Patrón Coreografía).
 */
@Component
public class VentaRealizadaBillingListener {

    private static final Logger log = LoggerFactory.getLogger(VentaRealizadaBillingListener.class);
    
    // Fallback DIAN para "Consumidor Final" cuando no hay NIT
    public static final String NIT_CONSUMIDOR_FINAL = "222222222222";
    public static final String NIT_EMISOR_DEFAULT = "900111222"; // Ejemplo, en la realidad vendría de la configuración del tenant

    private final EmitirFacturaUseCase emitirFacturaUseCase;

    public VentaRealizadaBillingListener(EmitirFacturaUseCase emitirFacturaUseCase) {
        this.emitirFacturaUseCase = emitirFacturaUseCase;
    }

    @Async
    @EventListener
    public void onVentaRegistrada(VentaRegistradaEvent event) {
        log.info("Billing: Interceptado VentaRegistradaEvent para emitir factura electrónica. Evento ID: {}", event.eventoId());

        // 1. Fallback Legal DIAN para Consumidor Final
        String nitReceptor = (event.clienteNit() != null && !event.clienteNit().isBlank()) 
                ? event.clienteNit() 
                : NIT_CONSUMIDOR_FINAL;

        // 2. Mapeo de Líneas
        List<EmitirFacturaCommand.LineaFacturaDto> lineas = event.lineas().stream()
                .map(l -> new EmitirFacturaCommand.LineaFacturaDto(
                        "Item " + l.productoId(), // Concepto deducido
                        new BigDecimal(l.cantidad()),
                        l.precioUnitario() != null ? l.precioUnitario() : BigDecimal.ZERO,
                        "COP", // Moneda local
                        List.of(new EmitirFacturaCommand.ImpuestoDto("IVA", new BigDecimal("19"))) // Asumiendo IVA 19%
                ))
                .collect(Collectors.toList());

        // 3. Crear Comando (Aislamiento MT-01 aplicado)
        EmitirFacturaCommand command = new EmitirFacturaCommand(
                event.empresaId().value(),
                NIT_EMISOR_DEFAULT, 
                nitReceptor,
                lineas
        );

        // 4. Ejecución
        try {
            emitirFacturaUseCase.emitirFactura(command);
            log.info("Factura electrónica orquestada exitosamente.");
        } catch (Exception e) {
            log.error("Falló la orquestación de la factura para el evento {}: {}", event.eventoId(), e.getMessage());
        }
    }
}
