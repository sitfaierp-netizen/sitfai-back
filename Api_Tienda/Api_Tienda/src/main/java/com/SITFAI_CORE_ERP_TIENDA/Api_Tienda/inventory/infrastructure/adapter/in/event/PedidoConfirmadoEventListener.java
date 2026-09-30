package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.in.event;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.pedido.event.PedidoConfirmadoEvent;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.pedido.LineaPedido;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.RegistrarMovimientoCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.Bodega;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.port.input.RegistrarMovimientoUseCase;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.port.output.BodegaRepository;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.EmpresaId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * Adaptador de Entrada Asincrónico / Event-Driven (Driving Adapter): Listener de Eventos de Dominio.
 * <p>
 * Pertenece exclusivamente a la Capa de Infraestructura del módulo {@code inventory} (REGLA-1).
 * Escucha eventos emitidos por el Bounded Context {@code Api_Tienda} y orquesta la actualización
 * de stock en {@code inventory} a través del puerto de entrada {@link RegistrarMovimientoUseCase}.
 * <p>
 * Reglas validadas:
 * <ul>
 *   <li>REGLA-1: El adaptador no contiene lógica de negocio; delega exclusivamente a la Application Layer.</li>
 *   <li>BOD-03: TipoMovimiento es {@code SALIDA}.</li>
 *   <li>BOD-04: Documento fuente es de tipo {@code PEDIDO} con el identificador del pedido.</li>
 *   <li>BOD-05: Protección contra stock negativo delegada y garantizada por el agregado Bodega.</li>
 *   <li>MT-01: Aislamiento estricto de multitenancy propagando {@code empresaId}.</li>
 * </ul>
 */
@Component("inventoryPedidoConfirmadoEventListener")
public class PedidoConfirmadoEventListener {

    private static final Logger log = LoggerFactory.getLogger(PedidoConfirmadoEventListener.class);

    private final RegistrarMovimientoUseCase registrarMovimientoUseCase;
    private final BodegaRepository bodegaRepository;

    public PedidoConfirmadoEventListener(
            RegistrarMovimientoUseCase registrarMovimientoUseCase,
            BodegaRepository bodegaRepository) {
        this.registrarMovimientoUseCase = Objects.requireNonNull(registrarMovimientoUseCase, "registrarMovimientoUseCase no puede ser null");
        this.bodegaRepository = Objects.requireNonNull(bodegaRepository, "bodegaRepository no puede ser null");
    }

    /**
     * Consume el evento de confirmación de pedido y registra la salida correspondiente en el inventario.
     *
     * @param event Evento de dominio emitido por Api_Tienda tras confirmar un Pedido.
     */
    @EventListener
    public void onPedidoConfirmado(PedidoConfirmadoEvent event) {
        Objects.requireNonNull(event, "PedidoConfirmadoEvent no puede ser null");

        log.info("Procesando PedidoConfirmadoEvent: pedidoId={}, empresaId={}, totalLineas={}",
                event.pedidoId().valor(), event.empresaId().valor(), event.lineas().size());

        EmpresaId empresaId = EmpresaId.de(event.empresaId().valor());

        // Resolver la Bodega activa del tenant para la salida de inventario
        List<Bodega> bodegasActivas = bodegaRepository.listarActivasPorEmpresa(empresaId);
        if (bodegasActivas.isEmpty()) {
            log.error("No se encontró ninguna Bodega activa para la Empresa {} al procesar pedido {}",
                    empresaId, event.pedidoId().valor());
            throw new IllegalStateException(
                    String.format("No existe una Bodega activa para la empresa '%s' para descontar inventario del pedido '%s'.",
                            empresaId, event.pedidoId().valor())
            );
        }

        Bodega bodega = bodegasActivas.get(0);
        String bodegaIdStr = bodega.getId().valor().toString();
        String empresaIdStr = event.empresaId().valor().toString();
        String pedidoIdStr = event.pedidoId().valor().toString();

        for (LineaPedido linea : event.lineas()) {
            RegistrarMovimientoCommand command = new RegistrarMovimientoCommand(
                    empresaIdStr,
                    bodegaIdStr,
                    linea.getProductoId().valor().toString(),
                    BigDecimal.valueOf(linea.getCantidad()),
                    "SALIDA",
                    "PEDIDO",
                    pedidoIdStr
            );

            log.debug("Registrando movimiento de salida: productoId={}, cantidad={}, docFuente={}",
                    linea.getProductoId().valor(), linea.getCantidad(), pedidoIdStr);

            registrarMovimientoUseCase.ejecutar(command);
        }

        log.info("Salida de inventario completada exitosamente para pedidoId={}", pedidoIdStr);
    }
}
