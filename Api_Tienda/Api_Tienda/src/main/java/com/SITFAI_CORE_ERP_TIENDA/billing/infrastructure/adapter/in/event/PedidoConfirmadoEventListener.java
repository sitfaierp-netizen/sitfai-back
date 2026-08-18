package com.SITFAI_CORE_ERP_TIENDA.billing.infrastructure.adapter.in.event;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.event.PedidoConfirmadoEvent;
import com.SITFAI_CORE_ERP_TIENDA.billing.application.dto.EmitirFacturaCommand;
import com.SITFAI_CORE_ERP_TIENDA.billing.application.dto.FacturaResponse;
import com.SITFAI_CORE_ERP_TIENDA.billing.application.port.input.EmitirFacturaUseCase;
import com.SITFAI_CORE_ERP_TIENDA.billing.application.port.output.FacturaRepository;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.valueobject.PedidoOrigenId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * Adaptador de Entrada Asincrónico / Event-Driven (Driving Adapter): Listener de Confirmación de Pedidos.
 * Emite facturas DIAN automáticamente al confirmar un pedido en Api_Tienda.
 */
@Component("billingPedidoConfirmadoEventListener")
public class PedidoConfirmadoEventListener {

    private static final Logger log = LoggerFactory.getLogger(PedidoConfirmadoEventListener.class);
    private static final String NIT_EMISOR_DEFAULT = "900111222";
    private static final String NIT_CONSUMIDOR_FINAL = "222222222222";

    private final EmitirFacturaUseCase emitirFacturaUseCase;
    private final FacturaRepository facturaRepository;

    public PedidoConfirmadoEventListener(
            EmitirFacturaUseCase emitirFacturaUseCase,
            FacturaRepository facturaRepository) {
        this.emitirFacturaUseCase = Objects.requireNonNull(emitirFacturaUseCase);
        this.facturaRepository = Objects.requireNonNull(facturaRepository);
    }

    @EventListener
    public void onPedidoConfirmado(PedidoConfirmadoEvent event) {
        Objects.requireNonNull(event, "PedidoConfirmadoEvent no puede ser null.");

        log.info("Procesando PedidoConfirmadoEvent en Billing: pedidoId={}, empresaId={}",
                event.pedidoId().valor(), event.empresaId().valor());

        EmpresaId empresaId = EmpresaId.de(event.empresaId().valor());
        PedidoOrigenId pedidoOrigenId = PedidoOrigenId.de(event.pedidoId().valor());

        if (facturaRepository.existePorPedidoOrigen(pedidoOrigenId, empresaId)) {
            log.warn("Factura ya existente para pedidoId={}. Omitiendo duplicación.", pedidoOrigenId.valor());
            return;
        }

        EmitirFacturaCommand.LineaFacturaDto linea = new EmitirFacturaCommand.LineaFacturaDto(
                "Pedido " + event.pedidoId().valor(),
                BigDecimal.ONE,
                event.total().monto(),
                event.total().moneda(),
                List.of(new EmitirFacturaCommand.ImpuestoDto("IVA", new BigDecimal("19")))
        );

        EmitirFacturaCommand command = new EmitirFacturaCommand(
                event.empresaId().valor(),
                NIT_EMISOR_DEFAULT,
                NIT_CONSUMIDOR_FINAL,
                List.of(linea)
        );

        FacturaResponse response = emitirFacturaUseCase.emitirFactura(command);

        log.info("Factura auto-emitida: facturaId={}", response.facturaId());
    }
}
