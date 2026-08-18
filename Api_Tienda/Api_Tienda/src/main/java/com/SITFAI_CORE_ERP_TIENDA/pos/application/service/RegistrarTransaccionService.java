package com.SITFAI_CORE_ERP_TIENDA.pos.application.service;

import com.SITFAI_CORE_ERP_TIENDA.pos.application.dto.RegistrarTransaccionCommand;
import com.SITFAI_CORE_ERP_TIENDA.pos.application.dto.TurnoCajaResponse;
import com.SITFAI_CORE_ERP_TIENDA.pos.application.mapper.TurnoCajaApplicationMapper;
import com.SITFAI_CORE_ERP_TIENDA.pos.application.port.input.RegistrarTransaccionUseCase;
import com.SITFAI_CORE_ERP_TIENDA.pos.application.port.output.TurnoCajaRepository;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.exception.TurnoNoEncontradoException;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.TipoTransaccion;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.TurnoCaja;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.valueobject.Dinero;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.valueobject.TurnoId;
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
        TipoTransaccion tipo = TipoTransaccion.valueOf(command.tipoTransaccion());
        Dinero monto = Dinero.de(command.monto());

        TurnoCaja turno = repository.buscarPorId(turnoId, empresaId)
                .orElseThrow(() -> new TurnoNoEncontradoException(turnoId, empresaId));

        turno.registrarTransaccion(tipo, monto, command.referencia());

        TurnoCaja turnoActualizado = repository.guardar(turno);

        return TurnoCajaApplicationMapper.toResponse(turnoActualizado);
    }
}
