package com.SITFAI_CORE_ERP_TIENDA.pos.application.service;

import com.SITFAI_CORE_ERP_TIENDA.pos.application.dto.RegistrarTransaccionCommand;
import com.SITFAI_CORE_ERP_TIENDA.pos.application.dto.TurnoCajaResponse;
import com.SITFAI_CORE_ERP_TIENDA.pos.application.mapper.TurnoCajaApplicationMapper;
import com.SITFAI_CORE_ERP_TIENDA.pos.application.port.input.RegistrarTransaccionUseCase;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.port.output.TurnoCajaRepository;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.exception.TurnoNoEncontradoException;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.turno.vo.TipoTransaccionCaja;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.turno.TurnoCaja;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.turno.vo.Dinero;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.turno.vo.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.turno.vo.TurnoId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
public class RegistrarTransaccionService implements RegistrarTransaccionUseCase {

    private final TurnoCajaRepository repository;

    public RegistrarTransaccionService(TurnoCajaRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    @Override
    @Transactional
    public TurnoCajaResponse ejecutar(RegistrarTransaccionCommand command) {
        Objects.requireNonNull(command);

        EmpresaId empresaId = new EmpresaId(command.empresaId());
        TurnoId turnoId = new TurnoId(command.turnoId());
        TipoTransaccionCaja tipo = TipoTransaccionCaja.valueOf(command.TipoTransaccionCaja());
        Dinero monto = Dinero.de(command.monto());

        TurnoCaja turno = repository.buscarPorId(turnoId, empresaId)
                .orElseThrow(() -> new TurnoNoEncontradoException(turnoId, empresaId));

        turno.registrarTransaccion(tipo, monto, command.referencia());

        TurnoCaja turnoActualizado = repository.guardar(turno, turno.getEmpresaId());

        return TurnoCajaApplicationMapper.toResponse(turnoActualizado);
    }
}
