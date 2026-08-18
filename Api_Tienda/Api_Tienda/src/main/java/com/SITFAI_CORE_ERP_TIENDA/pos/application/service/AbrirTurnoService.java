package com.SITFAI_CORE_ERP_TIENDA.pos.application.service;

import com.SITFAI_CORE_ERP_TIENDA.pos.application.dto.AbrirTurnoCommand;
import com.SITFAI_CORE_ERP_TIENDA.pos.application.dto.TurnoCajaResponse;
import com.SITFAI_CORE_ERP_TIENDA.pos.application.mapper.TurnoCajaApplicationMapper;
import com.SITFAI_CORE_ERP_TIENDA.pos.application.port.input.AbrirTurnoUseCase;
import com.SITFAI_CORE_ERP_TIENDA.pos.application.port.output.TurnoCajaRepository;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.TurnoCaja;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.valueobject.CajaId;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.valueobject.Dinero;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.valueobject.SucursalId;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.valueobject.UsuarioId;
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
        SucursalId sucursalId = new SucursalId(command.sucursalId());
        UsuarioId usuarioId = new UsuarioId(command.usuarioId());
        Dinero montoApertura = Dinero.de(command.montoApertura());

        TurnoCaja turno = TurnoCaja.abrirTurno(empresaId, cajaId, sucursalId, usuarioId, montoApertura);
        TurnoCaja turnoGuardado = repository.guardar(turno);

        return TurnoCajaApplicationMapper.toResponse(turnoGuardado);
    }
}
