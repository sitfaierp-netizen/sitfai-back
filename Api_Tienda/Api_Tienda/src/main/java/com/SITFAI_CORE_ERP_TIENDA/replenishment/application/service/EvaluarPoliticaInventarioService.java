package com.SITFAI_CORE_ERP_TIENDA.replenishment.application.service;

import com.SITFAI_CORE_ERP_TIENDA.replenishment.application.dto.EvaluarReposicionCommand;
import com.SITFAI_CORE_ERP_TIENDA.replenishment.application.port.input.EvaluarPoliticaInventarioUseCase;
import com.SITFAI_CORE_ERP_TIENDA.replenishment.domain.event.NecesidadAbastecimientoDetectadaEvent;
import com.SITFAI_CORE_ERP_TIENDA.replenishment.domain.model.politica.PoliticaInventario;
import com.SITFAI_CORE_ERP_TIENDA.replenishment.domain.model.politica.vo.BodegaId;
import com.SITFAI_CORE_ERP_TIENDA.replenishment.domain.model.politica.vo.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.replenishment.domain.model.politica.vo.ProductoId;
import com.SITFAI_CORE_ERP_TIENDA.replenishment.domain.port.output.PoliticaInventarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Caso de uso: Orquesta la evaluación de la política de inventario activa.
 * <p>
 * Regla MT-01: El empresaId es inyectado externamente (desde el token JWT), nunca del payload.
 * Regla REGLA-1: Este servicio solo llama a interfaces de dominio y puertos.
 * No contiene lógica de negocio — esa reside en el Agregado {@link PoliticaInventario}.
 */
@Service
public class EvaluarPoliticaInventarioService implements EvaluarPoliticaInventarioUseCase {

    private static final Logger log = LoggerFactory.getLogger(EvaluarPoliticaInventarioService.class);

    private final PoliticaInventarioRepository politicaRepository;
    private final ApplicationEventPublisher eventPublisher;

    public EvaluarPoliticaInventarioService(PoliticaInventarioRepository politicaRepository,
                                            ApplicationEventPublisher eventPublisher) {
        this.politicaRepository = politicaRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional(readOnly = true)
    public void evaluar(UUID empresaId, EvaluarReposicionCommand command) {
        EmpresaId tenant = new EmpresaId(empresaId);
        BodegaId bodegaId = new BodegaId(command.bodegaId());
        ProductoId productoId = new ProductoId(command.productoId());

        Optional<PoliticaInventario> politicaOpt = politicaRepository
                .buscarActivaPorBodegaYProducto(bodegaId, productoId, tenant);

        if (politicaOpt.isEmpty()) {
            log.debug("Replenishment: No existe política activa para bodega={}, producto={}, empresa={}",
                    command.bodegaId(), command.productoId(), empresaId);
            return;
        }

        PoliticaInventario politica = politicaOpt.get();
        politica.evaluarStock(command.stockDisponible());

        List<Object> events = politica.pullDomainEvents();
        if (events.isEmpty()) {
            log.debug("Replenishment: Stock={} no perfora el punto de reorden. bodega={}, producto={}",
                    command.stockDisponible(), command.bodegaId(), command.productoId());
            return;
        }

        events.forEach(event -> {
            if (event instanceof NecesidadAbastecimientoDetectadaEvent e) {
                log.info("Replenishment: ¡ALERTA DE REABASTECIMIENTO! empresa={}, bodega={}, producto={}, cantidadAReponer={}",
                        e.empresaId(), e.bodegaId(), e.productoId(), e.cantidadAReponer());
            }
            eventPublisher.publishEvent(event);
        });
    }
}
