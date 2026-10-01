package com.SITFAI_CORE_ERP_TIENDA.purchasing.application.service;

import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.dto.OrdenCompraResponse;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.dto.ProcesarPuntoReordenCommand;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.port.input.ProcesarPuntoReordenUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class ProcesarPuntoReordenService implements ProcesarPuntoReordenUseCase {

    private static final UUID PROVEEDOR_DEFAULT =
            UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final BigDecimal CANTIDAD_REPOSICION = new BigDecimal("50.0000");
    private static final BigDecimal COSTO_UNITARIO_TEMPORAL = new BigDecimal("15.0000");
    private static final String ACTOR_INTERNO = "system:punto-reorden";

    private final OrdenCompraApplicationOperation operation;

    public ProcesarPuntoReordenService(OrdenCompraApplicationOperation operation) {
        this.operation = operation;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public OrdenCompraResponse procesar(ProcesarPuntoReordenCommand command) {
        OrdenCompraResponse orden = operation.crearBorrador(
                command.empresaId(),
                PROVEEDOR_DEFAULT,
                ACTOR_INTERNO
        );

        return operation.agregarLinea(
                orden.id(),
                command.empresaId(),
                command.productoId(),
                CANTIDAD_REPOSICION,
                COSTO_UNITARIO_TEMPORAL,
                ACTOR_INTERNO
        );
    }
}
