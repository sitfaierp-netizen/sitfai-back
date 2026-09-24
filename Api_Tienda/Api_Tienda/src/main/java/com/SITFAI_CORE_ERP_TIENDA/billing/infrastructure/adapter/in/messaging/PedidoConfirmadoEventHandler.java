package com.SITFAI_CORE_ERP_TIENDA.billing.infrastructure.adapter.in.messaging;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.event.PedidoConfirmadoEvent;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.pedido.LineaPedido;
import com.SITFAI_CORE_ERP_TIENDA.billing.application.dto.EmitirFacturaCommand;
import com.SITFAI_CORE_ERP_TIENDA.billing.application.dto.FacturaResponse;
import com.SITFAI_CORE_ERP_TIENDA.billing.application.port.input.EmitirFacturaUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adaptador de Entrada Asincrónico (Messaging / Coreografía de Eventos):
 * Escucha el evento {@link PedidoConfirmadoEvent} emitido por el Bounded Context de E-Commerce (Api_Tienda)
 * y ejecuta el caso de uso {@link EmitirFacturaUseCase} para facturar automáticamente la venta.
 * <p>
 * Regla MT-01 (Multitenancy): El {@code empresa_id} se extrae DIRECTAMENTE del payload inmutable
 * del evento de dominio, sin utilizar contextos de seguridad HTTP ni hilos locales.
 * Regla 1 (Clean Architecture): Adaptador de entrada desacoplado del dominio mediante el caso de uso.
 */
@Component("billingPedidoConfirmadoEventHandler")
public class PedidoConfirmadoEventHandler {

    private static final Logger log = LoggerFactory.getLogger(PedidoConfirmadoEventHandler.class);

    private final EmitirFacturaUseCase emitirFacturaUseCase;

    public PedidoConfirmadoEventHandler(EmitirFacturaUseCase emitirFacturaUseCase) {
        this.emitirFacturaUseCase = Objects.requireNonNull(emitirFacturaUseCase, "EmitirFacturaUseCase es obligatorio");
    }

    @EventListener
    public void onPedidoConfirmado(PedidoConfirmadoEvent event) {
        Objects.requireNonNull(event, "PedidoConfirmadoEvent no puede ser nulo");

        log.info("Billing: Recibido PedidoConfirmadoEvent - Pedido: {}, Empresa: {}, Lineas: {}",
                event.pedidoId().valor(), event.empresaId().valor(), event.lineas().size());

        // Extracción directa de discriminadores inmutables desde el payload (MT-01 Zero Trust)
        UUID empresaId = event.empresaId().valor();
        UUID clienteId = event.clienteId().valor();
        UUID pedidoId = event.pedidoId().valor();

        // Mapeo seguro de líneas del pedido a líneas de factura comercial
        List<EmitirFacturaCommand.LineaFacturaCommand> lineasFactura = event.lineas().stream()
                .map(this::mapearLinea)
                .collect(Collectors.toList());

        EmitirFacturaCommand command = new EmitirFacturaCommand(
                empresaId,
                clienteId,
                pedidoId,
                "CONSUMIDOR_FINAL",
                lineasFactura,
                "ECOMMERCE",
                pedidoId
        );

        try {
            FacturaResponse response = emitirFacturaUseCase.emitirFactura(command);
            log.info("Billing: Factura {} emitida exitosamente para pedido {}",
                    response.id(), pedidoId);
        } catch (Exception e) {
            log.error("Billing: Error emitiendo factura para el pedido {}: {}",
                    pedidoId, e.getMessage(), e);
            throw e;
        }
    }

    private EmitirFacturaCommand.LineaFacturaCommand mapearLinea(LineaPedido linea) {
        String concepto = "Producto " + linea.getProductoId().valor();
        BigDecimal cantidad = BigDecimal.valueOf(linea.getCantidad());
        BigDecimal precioUnitario = linea.getPrecioUnitario().monto();
        String moneda = linea.getPrecioUnitario().moneda();

        // Impuesto base nacional (IVA 19%)
        List<EmitirFacturaCommand.ImpuestoCommand> impuestos = List.of(
                new EmitirFacturaCommand.ImpuestoCommand("IVA", new BigDecimal("19"))
        );

        return new EmitirFacturaCommand.LineaFacturaCommand(
                concepto,
                cantidad,
                precioUnitario,
                moneda,
                impuestos
        );
    }
}
