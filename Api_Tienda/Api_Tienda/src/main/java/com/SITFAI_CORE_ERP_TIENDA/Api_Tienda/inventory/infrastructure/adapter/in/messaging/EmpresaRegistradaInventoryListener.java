package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.in.messaging;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.CrearBodegaCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.port.input.CrearBodegaUseCase;
import com.SITFAI_CORE_ERP_TIENDA.shared.event.EmpresaRegistradaIntegrationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Objects;

@Component
public class EmpresaRegistradaInventoryListener {

    private static final Logger log = LoggerFactory.getLogger(EmpresaRegistradaInventoryListener.class);
    private final CrearBodegaUseCase crearBodegaUseCase;

    public EmpresaRegistradaInventoryListener(CrearBodegaUseCase crearBodegaUseCase) {
        this.crearBodegaUseCase = Objects.requireNonNull(crearBodegaUseCase);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @org.springframework.transaction.annotation.Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void onEmpresaRegistrada(EmpresaRegistradaIntegrationEvent event) {
        Objects.requireNonNull(event, "El evento de integración no puede ser nulo");
        log.info("Inventory Saga: Aprovisionando Bodega Principal para nueva EmpresaId={}", event.empresaId());

        CrearBodegaCommand command = new CrearBodegaCommand(
                event.empresaId().toString(),
                event.sucursalMatrizId().toString(),
                "BOD-MATRIZ",
                "Bodega Principal - " + event.razonSocial()
        );

        crearBodegaUseCase.ejecutar(command);
        log.info("Bodega aprovisionada exitosamente en Inventory.");
    }
}
