package com.SITFAI_CORE_ERP_TIENDA.pos.application.service;

import com.SITFAI_CORE_ERP_TIENDA.pos.application.dto.AbrirTurnoCommand;
import com.SITFAI_CORE_ERP_TIENDA.pos.application.dto.TurnoCajaResponse;
import com.SITFAI_CORE_ERP_TIENDA.pos.application.mapper.TurnoCajaApplicationMapper;
import com.SITFAI_CORE_ERP_TIENDA.pos.application.port.input.AbrirTurnoUseCase;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.port.output.TurnoCajaRepository;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.turno.TurnoCaja;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.turno.vo.CajaId;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.turno.vo.Dinero;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.turno.vo.EmpresaId;

import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.turno.vo.CajeroId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
public class AbrirTurnoService implements AbrirTurnoUseCase {

    private final TurnoCajaRepository repository;

    public AbrirTurnoService(TurnoCajaRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    @Override
    @Transactional
    public TurnoCajaResponse ejecutar(AbrirTurnoCommand command) {
        Objects.requireNonNull(command);

        EmpresaId empresaId = new EmpresaId(command.empresaId());
        CajaId cajaId = new CajaId(command.cajaId());
        
        CajeroId CajeroId = new CajeroId(command.CajeroId());
        Dinero montoApertura = Dinero.de(command.montoApertura());

        TurnoCaja turno = TurnoCaja.abrir(empresaId, cajaId, CajeroId, montoApertura);
        TurnoCaja turnoGuardado = repository.guardar(turno, turno.getEmpresaId());

        return TurnoCajaApplicationMapper.toResponse(turnoGuardado);
    }
}
