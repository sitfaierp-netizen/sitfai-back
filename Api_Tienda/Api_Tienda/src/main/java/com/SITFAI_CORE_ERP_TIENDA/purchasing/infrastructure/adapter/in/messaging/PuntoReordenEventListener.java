package com.SITFAI_CORE_ERP_TIENDA.purchasing.infrastructure.adapter.in.messaging;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.event.PuntoReordenAlcanzadoEvent;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.dto.AgregarLineaCommand;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.dto.CrearBorradorCommand;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.port.input.GestionarLineasUseCase;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.port.input.CrearOrdenUseCase;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.UUID;
import java.util.logging.Logger;

@Component
public class PuntoReordenEventListener {

    private static final Logger LOGGER = Logger.getLogger(PuntoReordenEventListener.class.getName());
    
    // Proveedor por defecto temporal (hasta implementar el servicio de resolución de proveedores)
    private static final UUID PROVEEDOR_DEFAULT = UUID.fromString("11111111-1111-1111-1111-111111111111");

    private final CrearOrdenUseCase crearOrdenUseCase;
    private final GestionarLineasUseCase gestionarLineasUseCase;

    public PuntoReordenEventListener(CrearOrdenUseCase crearOrdenUseCase, GestionarLineasUseCase gestionarLineasUseCase) {
        this.crearOrdenUseCase = crearOrdenUseCase;
        this.gestionarLineasUseCase = gestionarLineasUseCase;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePuntoReordenAlcanzadoEvent(PuntoReordenAlcanzadoEvent event) {
        LOGGER.info("Iniciando auto-replenishment para Producto: " + event.productoId().valor());

        UUID empresaId = event.empresaId().valor();
        UUID productoId = event.productoId().valor();
        UUID bodegaDestinoId = event.bodegaId().valor();

        // 1. Crear Orden de Compra (Borrador)
        CrearBorradorCommand crearCommand = new CrearBorradorCommand(empresaId, PROVEEDOR_DEFAULT, bodegaDestinoId);
        var ordenResponse = crearOrdenUseCase.crearBorrador(crearCommand);
        UUID ordenId = ordenResponse.id();

        // 2. Agregar línea de producto a la orden
        // Para la POC automatizada, se asume una cantidad estándar de reposición (ej. 50) y costo unitario base
        AgregarLineaCommand agregarCommand = new AgregarLineaCommand(
                ordenId,
                empresaId,
                productoId,
                new java.math.BigDecimal("50.0000"),
                new java.math.BigDecimal("15.0000") // Costo unitario mock temporal
        );
        
        gestionarLineasUseCase.agregarLinea(agregarCommand);
        
        LOGGER.info("Auto-replenishment completado. Creada OC: " + ordenId);
    }
}
