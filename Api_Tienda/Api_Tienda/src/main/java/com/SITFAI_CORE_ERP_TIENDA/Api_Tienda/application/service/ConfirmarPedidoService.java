package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.service;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.dto.ConfirmarPedidoCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.dto.PedidoResponse;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.mapper.PedidoApplicationMapper;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.port.input.ConfirmarPedidoUseCase;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.port.output.PedidoEventPublisher;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.port.output.PedidoRepository;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.event.DomainEvent;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.exception.PedidoNoEncontradoException;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.Pedido;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.PedidoId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * ═══════════════════════════════════════════════════════════════════════════════
 * APPLICATION SERVICE: Confirmar Pedido de Venta
 * ═══════════════════════════════════════════════════════════════════════════════
 * <p>
 * Orquesta la confirmación de un pedido respetando el aislamiento de responsabilidades:
 * <ol>
 *   <li><b>Multitenancy (MT-01, MT-02):</b> Recupera el agregado validando el tenant.</li>
 *   <li><b>Delegación al Dominio:</b> Invoca {@code pedido.confirmar()}, donde residen las invariantes
 *       (validación de líneas no vacías y estado de transición).</li>
 *   <li><b>Persistencia:</b> Guarda el estado del agregado confirmado.</li>
 *   <li><b>Eventos de Dominio (AUD-03):</b> Drena y publica los eventos acumulados hacia el SPI {@link PedidoEventPublisher}.</li>
 *   <li><b>DTO Mapping:</b> Transforma y retorna el resultado inmutable.</li>
 * </ol>
 * <p>
 * Cero lógica de negocio propia: toda regla de invariante pertenece al agregado {@link Pedido}.
 */
@Service("apiTiendaConfirmarPedidoService")
public class ConfirmarPedidoService implements ConfirmarPedidoUseCase {

    private final PedidoRepository pedidoRepository;
    private final PedidoEventPublisher eventPublisher;

    public ConfirmarPedidoService(
            PedidoRepository pedidoRepository,
            PedidoEventPublisher eventPublisher) {
        this.pedidoRepository = Objects.requireNonNull(pedidoRepository, "pedidoRepository es requerido.");
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "eventPublisher es requerido.");
    }

    @Override
    @Transactional
    public PedidoResponse ejecutar(ConfirmarPedidoCommand command) {
        Objects.requireNonNull(command, "ConfirmarPedidoCommand no puede ser null.");

        EmpresaId empresaId = EmpresaId.de(command.empresaId());
        PedidoId pedidoId = PedidoId.de(command.pedidoId());

        // 1. Recuperación con validación de Tenant (MT-01, MT-02)
        Pedido pedido = pedidoRepository.buscarPorId(pedidoId, empresaId)
                .orElseThrow(() -> new PedidoNoEncontradoException(pedidoId, empresaId));

        // 2. Delegar comportamiento e invariantes al Dominio (REGLA-1, REGLA-3)
        pedido.confirmar();

        // 3. Persistir el agregado actualizado
        Pedido pedidoConfirmado = pedidoRepository.guardar(pedido);

        // 4. Drenar y publicar Domain Events acumulados (AUD-03)
        List<DomainEvent> eventos = pedido.drainDomainEvents();
        eventPublisher.publicarTodos(eventos);

        // 5. Retornar DTO de respuesta
        return PedidoApplicationMapper.toResponse(pedidoConfirmado);
    }
}
