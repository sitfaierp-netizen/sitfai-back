package com.SITFAI_CORE_ERP_TIENDA.core_empresa.infrastructure.adapter.out.event;

import com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.port.output.EmpresaEventPublisher;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.event.DomainEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SpringEventEmpresaPublisher implements EmpresaEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public SpringEventEmpresaPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void publicar(DomainEvent event) {
        applicationEventPublisher.publishEvent(event);

        if (event instanceof com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.event.EmpresaCreadaEvent e) {
            applicationEventPublisher.publishEvent(
                    new com.SITFAI_CORE_ERP_TIENDA.shared.event.EmpresaRegistradaIntegrationEvent(
                            e.empresaId().valor(),
                            e.sucursalPrincipalId().valor(),
                            e.ruc().valor(),
                            e.nombre().valor()
                    )
            );
        }
    }

    @Override
    public void publicarTodos(List<DomainEvent> events) {
        events.forEach(this::publicar);
    }
}
