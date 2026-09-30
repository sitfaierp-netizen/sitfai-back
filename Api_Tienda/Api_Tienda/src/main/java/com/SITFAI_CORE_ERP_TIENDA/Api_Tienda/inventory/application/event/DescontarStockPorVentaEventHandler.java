package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.event;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.DescontarStockCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.port.input.DescontarStockUseCase;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.Bodega;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.port.output.BodegaRepository;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.event.TicketEmitidoEvent;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.LineaTicket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.Optional;

@Component
public class DescontarStockPorVentaEventHandler {

    private static final Logger log = LoggerFactory.getLogger(DescontarStockPorVentaEventHandler.class);
    private final DescontarStockUseCase descontarStockUseCase;
    private final BodegaRepository bodegaRepository;

    public DescontarStockPorVentaEventHandler(DescontarStockUseCase descontarStockUseCase, BodegaRepository bodegaRepository) {
        this.descontarStockUseCase = descontarStockUseCase;
        this.bodegaRepository = bodegaRepository;
    }

    /**
     * Escucha el evento TicketEmitidoEvent.
     * Usamos AFTER_COMMIT para garantizar que el ticket se persistió correctamente
     * antes de afectar el inventario. Al no ser @Async por defecto, se ejecuta
     * en el mismo hilo, por lo que el SecurityContext sigue presente (TenantProviderPort funciona).
     * Requiere REQUIRES_NEW si queremos que abra una transacción independiente
     * dado que la actual ya hizo commit.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void on(TicketEmitidoEvent event) {
        log.info("Procesando evento TicketEmitidoEvent para ticket {} de empresa {}", event.ticketId(), event.empresaId());

        EmpresaId empresaId = new EmpresaId(event.empresaId());
        
        List<Bodega> bodegas = bodegaRepository.listarActivasPorEmpresa(empresaId);
        Optional<Bodega> bodegaVentaOpt = bodegas.stream()
                .filter(Bodega::puedeVender)
                .findFirst();

        if (bodegaVentaOpt.isEmpty()) {
            log.error("No se encontró una Bodega activa de tipo VENTA para la empresa {}", empresaId.valor());
            return;
        }

        Bodega bodega = bodegaVentaOpt.get();

        for (LineaTicket linea : event.lineas()) {
            DescontarStockCommand command = new DescontarStockCommand(
                    bodega.getId().valor(),
                    linea.getProductoId().value(),
                    linea.getCantidad(),
                    "TICKET_VENTA",
                    event.ticketId().value().toString(),
                    event.empresaId()
            );
            
            try {
                descontarStockUseCase.descontarStock(command);
                log.info("Stock descontado para producto {} en bodega {}", linea.getProductoId().value(), bodega.getId().valor());
            } catch (Exception e) {
                log.error("Error al descontar stock para producto {} en ticket {}", linea.getProductoId().value(), event.ticketId().value(), e);
                // Si la BD lanza ObjectOptimisticLockingFailureException u otra excepción,
                // se detiene el procesamiento de esta línea, pero al ser AFTER_COMMIT
                // no hace rollback del TicketVenta (cumpliendo acoplamiento mínimo y resiliencia parcial).
                throw e; // Lanzar para que la transacción de inventario haga rollback
            }
        }
    }
}
