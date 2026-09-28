package com.SITFAI_CORE_ERP_TIENDA.pos.application.service;

import com.SITFAI_CORE_ERP_TIENDA.pos.application.dto.RegistrarTransaccionCajaCommand;
import com.SITFAI_CORE_ERP_TIENDA.pos.application.dto.TurnoCajaResponse;
import com.SITFAI_CORE_ERP_TIENDA.pos.application.port.input.RegistrarTransaccionCajaUseCase;
import com.SITFAI_CORE_ERP_TIENDA.pos.application.port.output.TenantProviderPort;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.event.VentaRegistradaEvent;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.turno.TurnoCaja;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.turno.vo.Dinero;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.turno.vo.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.turno.vo.TipoTransaccionCaja;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.turno.vo.TurnoId;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.port.output.TurnoCajaRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class RegistrarTransaccionCajaService implements RegistrarTransaccionCajaUseCase {

    private final TurnoCajaRepository turnoCajaRepository;
    private final TenantProviderPort tenantProviderPort;
    private final ApplicationEventPublisher eventPublisher;

    public RegistrarTransaccionCajaService(
            TurnoCajaRepository turnoCajaRepository,
            TenantProviderPort tenantProviderPort,
            ApplicationEventPublisher eventPublisher
    ) {
        this.turnoCajaRepository = Objects.requireNonNull(turnoCajaRepository);
        this.tenantProviderPort = Objects.requireNonNull(tenantProviderPort);
        this.eventPublisher = Objects.requireNonNull(eventPublisher);
    }

    @Override
    public TurnoCajaResponse registrarTransaccion(RegistrarTransaccionCajaCommand command) {
        Objects.requireNonNull(command, "El comando no puede ser nulo");

        UUID tenantUuid = tenantProviderPort.getEmpresaIdAutenticada().value();
        EmpresaId empresaId = EmpresaId.of(tenantUuid);
        TurnoId turnoId = TurnoId.of(command.turnoId());

        TurnoCaja turno = turnoCajaRepository.buscarPorId(turnoId, empresaId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el turno de caja"));

        TipoTransaccionCaja tipo = TipoTransaccionCaja.valueOf(command.TipoTransaccionCaja().toUpperCase());
        Dinero monto = Dinero.de(command.monto());

        UUID transaccionId = turno.registrarTransaccion(tipo, monto, command.referencia());

        TurnoCaja guardado = turnoCajaRepository.guardar(turno, empresaId);

        if (tipo == TipoTransaccionCaja.VENTA) {
            List<VentaRegistradaEvent.LineaVenta> lineasEvento = command.lineas() != null ?
                    command.lineas().stream()
                            .map(l -> new VentaRegistradaEvent.LineaVenta(l.productoId(), l.cantidad(), l.precioUnitario()))
                            .collect(Collectors.toList())
                    : Collections.emptyList();

            VentaRegistradaEvent evento = VentaRegistradaEvent.of(
                    new com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.turno.vo.EmpresaId(empresaId.value()),
                    turno.getId().value(),
                    turno.getCajaId().value(),
                    "CONSUMIDOR_FINAL",
                    command.referencia(),
                    lineasEvento
            );
            eventPublisher.publishEvent(evento);
        }

        return mapearAResponse(guardado);
    }

    private TurnoCajaResponse mapearAResponse(TurnoCaja turno) {
        var arqueo = turno.getArqueo();
        return new TurnoCajaResponse(
                turno.getId().value(),
                turno.getEmpresaId().value(),
                turno.getCajaId().value(),
                null,
                turno.getCajeroId().value(),
                turno.getEstado().name(),
                turno.getMontoApertura().monto(),
                (arqueo != null) ? arqueo.totalTeoricoEsperado().monto() : turno.calcularTotalTeorico().monto(),
                Collections.emptyList()
        );
    }
}
