package com.SITFAI_CORE_ERP_TIENDA.pos.application.service;

import com.SITFAI_CORE_ERP_TIENDA.pos.application.dto.AbrirTurnoCommand;
import com.SITFAI_CORE_ERP_TIENDA.pos.application.dto.CerrarTurnoCommand;
import com.SITFAI_CORE_ERP_TIENDA.pos.application.dto.TurnoCajaResponse;
import com.SITFAI_CORE_ERP_TIENDA.pos.application.port.input.AbrirTurnoUseCase;
import com.SITFAI_CORE_ERP_TIENDA.pos.application.port.input.CerrarTurnoUseCase;
import com.SITFAI_CORE_ERP_TIENDA.pos.application.port.input.GestionarTurnoCajaUseCase;
import com.SITFAI_CORE_ERP_TIENDA.pos.application.port.output.CurrentActorProvider;
import com.SITFAI_CORE_ERP_TIENDA.pos.application.port.output.TenantProviderPort;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.turno.TurnoCaja;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.turno.vo.CajaId;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.turno.vo.CajeroId;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.turno.vo.Dinero;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.turno.vo.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.turno.vo.TurnoId;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.port.output.TurnoCajaRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Objects;
import java.util.UUID;

/**
 * Servicio de Aplicación transaccional para la gestión del ciclo de vida de los Turnos de Caja (POS).
 * <p>
 * Regla MT-01 / MT-02: Obtiene el Tenant de forma estricta mediante {@link TenantProviderPort}.
 * Regla AUD-01: Obtiene el actor para auditoría mediante {@link CurrentActorProvider}.
 * Regla CAJ-02 a CAJ-07: Orquesta apertura, cierre con arqueo inmutable y publicación de eventos.
 */
@Service
@Transactional
public class GestionarTurnoCajaService implements GestionarTurnoCajaUseCase, AbrirTurnoUseCase, CerrarTurnoUseCase {

    private final TurnoCajaRepository turnoCajaRepository;
    private final TenantProviderPort tenantProviderPort;
    private final CurrentActorProvider currentActorProvider;
    private final ApplicationEventPublisher eventPublisher;

    public GestionarTurnoCajaService(
            TurnoCajaRepository turnoCajaRepository,
            TenantProviderPort tenantProviderPort,
            CurrentActorProvider currentActorProvider,
            ApplicationEventPublisher eventPublisher
    ) {
        this.turnoCajaRepository = Objects.requireNonNull(turnoCajaRepository, "TurnoCajaRepository es obligatorio");
        this.tenantProviderPort = Objects.requireNonNull(tenantProviderPort, "TenantProviderPort es obligatorio");
        this.currentActorProvider = Objects.requireNonNull(currentActorProvider, "CurrentActorProvider es obligatorio");
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "ApplicationEventPublisher es obligatorio");
    }

    @Override
    public TurnoCajaResponse abrirTurno(AbrirTurnoCommand command) {
        Objects.requireNonNull(command, "AbrirTurnoCommand no puede ser nulo");

        UUID tenantUuid = (command.empresaId() != null)
                ? command.empresaId()
                : tenantProviderPort.getEmpresaIdAutenticada().value();

        EmpresaId empresaId = EmpresaId.of(tenantUuid);
        CajaId cajaId = CajaId.of(command.cajaId());
        CajeroId cajeroId = CajeroId.of(command.cajeroId());

        // Validar si la caja ya cuenta con un turno abierto
        turnoCajaRepository.buscarTurnoAbiertoPorCaja(cajaId, empresaId).ifPresent(t -> {
            throw new IllegalStateException("La caja " + command.cajaId() + " ya tiene un turno abierto (ID: " + t.getId().value() + ")");
        });

        // Instanciar Agregado a través del Factory Method de Dominio
        Dinero apertura = Dinero.de(command.montoApertura());
        TurnoCaja nuevoTurno = TurnoCaja.abrir(empresaId, cajaId, cajeroId, apertura);

        // Guardar turno en repositorio
        TurnoCaja guardado = turnoCajaRepository.guardar(nuevoTurno, empresaId);

        return mapearAResponse(guardado);
    }

    @Override
    public TurnoCajaResponse cerrarTurno(CerrarTurnoCommand command) {
        Objects.requireNonNull(command, "CerrarTurnoCommand no puede ser nulo");

        UUID tenantUuid = (command.empresaId() != null)
                ? command.empresaId()
                : tenantProviderPort.getEmpresaIdAutenticada().value();

        EmpresaId empresaId = EmpresaId.of(tenantUuid);
        TurnoId turnoId = TurnoId.of(command.turnoId());

        // Cargar turno desde el repositorio
        TurnoCaja turno = turnoCajaRepository.buscarPorId(turnoId, empresaId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el turno de caja con ID: " + command.turnoId()));

        // Ejecutar cierre en el agregado (calcula arqueo, descuadre y emite TurnoCerradoEvent)
        Dinero montoDeclarado = Dinero.de(command.montoFisicoDeclarado());
        turno.cerrar(montoDeclarado);

        // Persistir el agregado modificado con su arqueo inmutable
        TurnoCaja guardado = turnoCajaRepository.guardar(turno, empresaId);

        // Despachar eventos de dominio acumulados
        for (Object evento : turno.getDomainEvents()) {
            eventPublisher.publishEvent(evento);
        }


        return mapearAResponse(guardado);
    }

    // Sobrecargas de compatibilidad con interfaces individuales
    @Override
    public TurnoCajaResponse ejecutar(AbrirTurnoCommand command) {
        return abrirTurno(command);
    }

    @Override
    public TurnoCajaResponse ejecutar(CerrarTurnoCommand command) {
        return cerrarTurno(command);
    }

    private TurnoCajaResponse mapearAResponse(TurnoCaja turno) {
        var arqueo = turno.getArqueo();
        return new TurnoCajaResponse(
                turno.getId().value(),
                turno.getEmpresaId().value(),
                turno.getCajaId().value(),
                null, // sucursalId opcional
                turno.getCajeroId().value(),
                turno.getEstado().name(),
                turno.getMontoApertura().monto(),
                (arqueo != null) ? arqueo.totalTeoricoEsperado().monto() : turno.calcularTotalTeorico().monto(),
                Collections.emptyList()
        );
    }
}
