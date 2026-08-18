package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.in.messaging;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.event.StockActualizadoEvent;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.out.persistence.cqrs.StockProyeccionId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.out.persistence.cqrs.StockProyeccionJpaEntity;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.out.persistence.cqrs.StockProyeccionJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Proyector CQRS: Escucha los eventos del modelo de escritura de inventario
 * y actualiza de manera desnormalizada la tabla de lectura.
 */
@Component
public class StockConsolidadoProjector {

    private static final Logger log = LoggerFactory.getLogger(StockConsolidadoProjector.class);
    private final StockProyeccionJpaRepository readRepository;

    public StockConsolidadoProjector(StockProyeccionJpaRepository readRepository) {
        this.readRepository = readRepository;
    }

    /**
     * Reacciona de forma asíncrona al evento de actualización de stock.
     */
    @Async
    @EventListener
    @Transactional
    public void onStockActualizado(StockActualizadoEvent event) {
        log.debug("Proyectando stock para producto={}, bodega={}, empresa={}",
                event.productoId().valor(), event.bodegaId().valor(), event.empresaId().valor());

        StockProyeccionId id = new StockProyeccionId(
                event.empresaId().valor(),
                event.bodegaId().valor(),
                event.productoId().valor()
        );

        StockProyeccionJpaEntity entity = readRepository.findById(id)
                .orElse(new StockProyeccionJpaEntity(
                        event.empresaId().valor(),
                        event.bodegaId().valor(),
                        event.productoId().valor(),
                        event.stockNuevo(),
                        event.ocurridoEn()
                ));

        entity.setCantidadTotal(event.stockNuevo());
        entity.setUltimaActualizacion(event.ocurridoEn());

        readRepository.save(entity);
        log.info("Proyección CQRS de stock actualizada correctamente.");
    }
}
