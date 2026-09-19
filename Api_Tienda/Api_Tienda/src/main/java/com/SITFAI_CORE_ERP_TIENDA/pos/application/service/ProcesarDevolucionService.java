package com.SITFAI_CORE_ERP_TIENDA.pos.application.service;

import com.SITFAI_CORE_ERP_TIENDA.pos.application.dto.ProcesarDevolucionCommand;
import com.SITFAI_CORE_ERP_TIENDA.pos.application.port.input.ProcesarDevolucionUseCase;
import com.SITFAI_CORE_ERP_TIENDA.pos.application.port.output.CurrentActorProvider;
import com.SITFAI_CORE_ERP_TIENDA.pos.application.port.output.TenantProviderPort;
import com.SITFAI_CORE_ERP_TIENDA.pos.application.port.output.TurnoCajaRepository;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.event.DevolucionRegistradaEvent;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.TurnoCaja;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.vo.LoteRevertido;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.vo.ProductoId;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.vo.TicketId;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.valueobject.Dinero;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.valueobject.TurnoId;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ProcesarDevolucionService implements ProcesarDevolucionUseCase {

    private final TurnoCajaRepository repository;
    private final TenantProviderPort tenantProvider;
    private final CurrentActorProvider actorProvider;
    private final ApplicationEventPublisher eventPublisher;

    public ProcesarDevolucionService(TurnoCajaRepository repository, 
                                     TenantProviderPort tenantProvider, 
                                     CurrentActorProvider actorProvider,
                                     ApplicationEventPublisher eventPublisher) {
        this.repository = Objects.requireNonNull(repository);
        this.tenantProvider = Objects.requireNonNull(tenantProvider);
        this.actorProvider = Objects.requireNonNull(actorProvider);
        this.eventPublisher = Objects.requireNonNull(eventPublisher);
    }

    @Override
    @Transactional
    public void ejecutar(ProcesarDevolucionCommand command) {
        // MT-01: Extraer EmpresaId desde el contexto de seguridad, nunca del request (cero confianza)
        EmpresaId empresaAutenticada = tenantProvider.getEmpresaIdAutenticada();
        
        // AUD-01: Trazabilidad del actor
        String actor = actorProvider.getActorActual();

        // 1. Recuperar TurnoCaja
        TurnoId turnoId = new TurnoId(command.turnoId());
        TurnoCaja turno = repository.buscarPorId(turnoId, empresaAutenticada)
                .orElseThrow(() -> new IllegalArgumentException("Turno de caja no encontrado"));

        // MT-02: Aislamiento Multitenant (el turno debe pertenecer a la empresa autenticada)
        if (!turno.getEmpresaId().equals(empresaAutenticada)) {
            throw new SecurityException("Violación de Tenant (MT-02): El turno no pertenece a la empresa autenticada");
        }

        // 2. Mapear DTOs a Value Objects
        TicketId ticketId = new TicketId(command.ticketOriginalId());
        Dinero montoDevolucion = Dinero.de(command.montoDevuelto());
        
        List<DevolucionRegistradaEvent.LineaDevolucion> lineas = command.lineas().stream()
                .map(l -> new DevolucionRegistradaEvent.LineaDevolucion(l.productoId(), l.cantidad(), l.precioUnitario()))
                .collect(Collectors.toList());

        List<LoteRevertido> lotes = command.lotesRevertidos().stream()
                .map(l -> new LoteRevertido(new ProductoId(l.productoId()), l.codigoLote(), l.cantidad()))
                .collect(Collectors.toList());

        // 3. Ejecutar Lógica de Dominio
        turno.registrarDevolucion(ticketId, montoDevolucion, lineas, lotes);

        // 4. Persistir estado y publicar eventos
        TurnoCaja turnoActualizado = repository.guardar(turno);
        
        turnoActualizado.getDomainEvents().forEach(eventPublisher::publishEvent);
        turnoActualizado.getDomainEvents().clear();
    }
}
