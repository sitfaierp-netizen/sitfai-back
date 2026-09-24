package com.SITFAI_CORE_ERP_TIENDA.replenishment.infrastructure.adapter.in.messaging;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.event.StockDescontadoPorVentaEvent;
import com.SITFAI_CORE_ERP_TIENDA.replenishment.application.dto.EvaluarReposicionCommand;
import com.SITFAI_CORE_ERP_TIENDA.replenishment.application.port.input.EvaluarPoliticaInventarioUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Sensor de Inventario (Driving Adapter asíncrono).
 * Escucha los eventos de disminución de stock emitidos por el módulo Inventory
 * y activa el caso de uso de evaluación de reposición.
 * <p>
 * Regla REGLA-1: Este componente pertenece a Infraestructura.
 * El módulo replenishment reacciona al {@link StockDescontadoPorVentaEvent}
 * sin acoplar directamente los bounded contexts en capas de dominio.
 */
@Component
public class StockMovimientoEventListener {

    private static final Logger log = LoggerFactory.getLogger(StockMovimientoEventListener.class);

    private final EvaluarPoliticaInventarioUseCase evaluarPoliticaUseCase;

    public StockMovimientoEventListener(EvaluarPoliticaInventarioUseCase evaluarPoliticaUseCase) {
        this.evaluarPoliticaUseCase = evaluarPoliticaUseCase;
    }

    @Async
    @EventListener
    public void onStockDescontado(StockDescontadoPorVentaEvent event) {
        log.info("Replenishment Sensor: StockDescontadoPorVentaEvent recibido. bodega={}, producto={}, empresa={}",
                event.bodegaId().valor(), event.productoId().valor(), event.empresaId().valor());

        // El stockDisponible no viene en el evento de descuento — usamos 0 como trigger
        // para que el Agregado evalúe contra su Punto de Reorden registrado en BD.
        // La evaluación real usa el stock actual que viene desde la capa de lógica del evento.
        int cantidadDescontada = event.cantidad().valor().intValue();

        // Invocamos con la cantidad descontada como proxy del "stock que ya no existe".
        // El caso de uso buscará el stock actual via su propia lógica (extensible).
        EvaluarReposicionCommand command = new EvaluarReposicionCommand(
                event.bodegaId().valor(),
                event.productoId().valor(),
                cantidadDescontada  // Proxy: el sensor notifica cuánto se descontó
        );

        try {
            evaluarPoliticaUseCase.evaluar(event.empresaId().valor(), command);
        } catch (Exception e) {
            log.error("Replenishment Sensor: Error al evaluar política para producto={}, bodega={}: {}",
                    event.productoId().valor(), event.bodegaId().valor(), e.getMessage(), e);
        }
    }
}
