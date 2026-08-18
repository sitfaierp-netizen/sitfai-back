package com.SITFAI_CORE_ERP_TIENDA.fulfillment.application.service;

import com.SITFAI_CORE_ERP_TIENDA.fulfillment.application.dto.CompletarPackingCommand;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.application.dto.OrdenDespachoResponse;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.application.mapper.OrdenDespachoApplicationMapper;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.application.port.input.CompletarPackingUseCase;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.application.port.output.OrdenDespachoEventPublisher;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.application.port.output.OrdenDespachoRepository;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.event.DomainEvent;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.exception.DespachoNoEncontradoException;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.model.OrdenDespacho;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.valueobject.DespachoId;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.valueobject.EmpresaId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class CompletarPackingService implements CompletarPackingUseCase {

    private final OrdenDespachoRepository repository;
    private final OrdenDespachoEventPublisher eventPublisher;

    public CompletarPackingService(OrdenDespachoRepository repository, OrdenDespachoEventPublisher eventPublisher) {
        this.repository = Objects.requireNonNull(repository);
        this.eventPublisher = Objects.requireNonNull(eventPublisher);
    }

    @Override
    @Transactional
    public OrdenDespachoResponse ejecutar(CompletarPackingCommand command) {
        Objects.requireNonNull(command);

        EmpresaId empresaId = new EmpresaId(command.empresaId());
        DespachoId despachoId = new DespachoId(command.despachoId());

        OrdenDespacho orden = repository.buscarPorId(despachoId, empresaId)
                .orElseThrow(() -> new DespachoNoEncontradoException(despachoId, empresaId));

        orden.completarPacking();

        OrdenDespacho ordenActualizada = repository.guardar(orden);

        List<DomainEvent> eventos = ordenActualizada.drainDomainEvents();
        eventPublisher.publicarTodos(eventos);

        return OrdenDespachoApplicationMapper.toResponse(ordenActualizada);
    }
}
