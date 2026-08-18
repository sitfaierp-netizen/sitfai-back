package com.SITFAI_CORE_ERP_TIENDA.pos.application.service;

import com.SITFAI_CORE_ERP_TIENDA.pos.application.dto.CerrarTurnoCommand;
import com.SITFAI_CORE_ERP_TIENDA.pos.application.dto.TurnoCajaResponse;
import com.SITFAI_CORE_ERP_TIENDA.pos.application.mapper.TurnoCajaApplicationMapper;
import com.SITFAI_CORE_ERP_TIENDA.pos.application.port.input.CerrarTurnoUseCase;
import com.SITFAI_CORE_ERP_TIENDA.pos.application.port.output.TurnoCajaEventPublisher;
import com.SITFAI_CORE_ERP_TIENDA.pos.application.port.output.TurnoCajaRepository;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.event.DomainEvent;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.exception.TurnoNoEncontradoException;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.TurnoCaja;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.valueobject.Dinero;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.valueobject.TurnoId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class CerrarTurnoService implements CerrarTurnoUseCase {

    private final TurnoCajaRepository repository;
    private final TurnoCajaEventPublisher eventPublisher;

    public CerrarTurnoService(TurnoCajaRepository repository, TurnoCajaEventPublisher eventPublisher) {
        this.repository = Objects.requireNonNull(repository);
        this.eventPublisher = Objects.requireNonNull(eventPublisher);
    }

    @Override
    @Transactional
    public TurnoCajaResponse ejecutar(CerrarTurnoCommand command) {
        Objects.requireNonNull(command);

        EmpresaId empresaId = new EmpresaId(command.empresaId());
        TurnoId turnoId = new TurnoId(command.turnoId());
        Dinero montoDeclarado = Dinero.de(command.montoDeclarado());

        TurnoCaja turno = repository.buscarPorId(turnoId, empresaId)
                .orElseThrow(() -> new TurnoNoEncontradoException(turnoId, empresaId));

        turno.cerrarTurno(montoDeclarado);

        TurnoCaja turnoActualizado = repository.guardar(turno);

        // Extracción de eventos de dominio
        List<DomainEvent> eventos = new ArrayList<>(turnoActualizado.getDomainEvents());
        // En una implementación real con AggregateRoot base class, tendríamos un método clearDomainEvents()
        
        eventPublisher.publicarTodos(eventos);

        return TurnoCajaApplicationMapper.toResponse(turnoActualizado);
    }
}
