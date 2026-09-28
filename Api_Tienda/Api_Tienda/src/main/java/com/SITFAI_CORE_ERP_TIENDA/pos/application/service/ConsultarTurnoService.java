package com.SITFAI_CORE_ERP_TIENDA.pos.application.service;

import com.SITFAI_CORE_ERP_TIENDA.pos.application.dto.TurnoCajaResponse;
import com.SITFAI_CORE_ERP_TIENDA.pos.application.mapper.TurnoCajaApplicationMapper;
import com.SITFAI_CORE_ERP_TIENDA.pos.application.port.input.ConsultarTurnoUseCase;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.port.output.TurnoCajaRepository;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.exception.TurnoNoEncontradoException;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.turno.TurnoCaja;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.turno.vo.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.turno.vo.TurnoId;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.UUID;

@Service
public class ConsultarTurnoService implements ConsultarTurnoUseCase {

    private final TurnoCajaRepository repository;

    public ConsultarTurnoService(TurnoCajaRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    @Override
    public TurnoCajaResponse porId(UUID id, UUID empresaId) {
        TurnoId turnoId = new TurnoId(id);
        EmpresaId empId = new EmpresaId(empresaId);

        TurnoCaja turno = repository.buscarPorId(turnoId, empId)
                .orElseThrow(() -> new TurnoNoEncontradoException(turnoId, empId));

        return TurnoCajaApplicationMapper.toResponse(turno);
    }
}
