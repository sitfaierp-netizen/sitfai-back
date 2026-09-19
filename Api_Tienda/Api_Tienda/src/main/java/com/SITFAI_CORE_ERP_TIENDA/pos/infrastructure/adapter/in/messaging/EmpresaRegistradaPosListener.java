package com.SITFAI_CORE_ERP_TIENDA.pos.infrastructure.adapter.in.messaging;

import com.SITFAI_CORE_ERP_TIENDA.pos.application.dto.CrearCajaCommand;
import com.SITFAI_CORE_ERP_TIENDA.pos.application.port.input.CrearCajaUseCase;
import com.SITFAI_CORE_ERP_TIENDA.shared.event.EmpresaRegistradaIntegrationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Objects;

@Component
public class EmpresaRegistradaPosListener {

    private static final Logger log = LoggerFactory.getLogger(EmpresaRegistradaPosListener.class);
    private final CrearCajaUseCase crearCajaUseCase;

    public EmpresaRegistradaPosListener(CrearCajaUseCase crearCajaUseCase) {
        this.crearCajaUseCase = Objects.requireNonNull(crearCajaUseCase);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @org.springframework.transaction.annotation.Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void onEmpresaRegistrada(EmpresaRegistradaIntegrationEvent event) {
        Objects.requireNonNull(event, "El evento de integración no puede ser nulo");
        log.info("POS Saga: Aprovisionando Caja Principal para nueva EmpresaId={}", event.empresaId());

        CrearCajaCommand command = new CrearCajaCommand(
                event.empresaId(),
                event.sucursalMatrizId(),
                "Caja Principal"
        );

        crearCajaUseCase.ejecutar(command);
        log.info("Caja aprovisionada exitosamente en POS.");
    }
}
