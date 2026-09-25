package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.service;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.dto.ConfirmarPedidoCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.dto.PedidoResponse;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.mapper.PedidoApplicationMapper;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.port.input.ConfirmarPedidoUseCase;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.port.output.PedidoEventPublisher;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.exception.PedidoNoEncontradoException;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.pedido.vo.EstadoPedido;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.pedido.Pedido;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.port.output.PedidoRepository;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.pedido.vo.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.pedido.vo.PedidoId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * Caso de Uso (Application Service): Confirmar Pedido.
 * <p>
 * Orquesta la confirmaciÃ³n del Pedido bajo aislamiento estricto Multi-Tenant (MT-01).
 * Soporta tanto la confirmaciÃ³n originada por la SAGA de reserva de stock (Happy Path)
 * como la confirmaciÃ³n directa.
 * <p>
 * Reglas validadas: REGLA-1 (Application Layer), MT-01 (Aislamiento por EmpresaId), AUD-03 (Eventos de Dominio).
 */
@Service("apiTiendaConfirmarPedidoService")
@Transactional
public class ConfirmarPedidoService implements ConfirmarPedidoUseCase {

    private final PedidoRepository pedidoRepository;
    private final PedidoEventPublisher eventPublisher;

    public ConfirmarPedidoService(PedidoRepository pedidoRepository, PedidoEventPublisher eventPublisher) {
        this.pedidoRepository = Objects.requireNonNull(pedidoRepository, "pedidoRepository es obligatorio");
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "eventPublisher es obligatorio");
    }

    @Override
    public PedidoResponse ejecutar(ConfirmarPedidoCommand command) {
        PedidoId pedidoId = PedidoId.de(command.pedidoId());
        EmpresaId empresaId = EmpresaId.de(command.empresaId());

        // MT-01: BÃºsqueda obligatoriamente acotada por tenant
        Pedido pedido = pedidoRepository.buscarPorId(pedidoId, empresaId)
                .orElseThrow(() -> new PedidoNoEncontradoException(pedidoId, empresaId));

        if (pedido.getEstado() == EstadoPedido.RESERVANDO_STOCK) {
            pedido.confirmarReserva();
        } else {
            pedido.confirmar();
        }

        pedidoRepository.guardar(pedido);

        // PublicaciÃ³n de Domain Events acumulados (PedidoConfirmadoEvent)
        eventPublisher.publicarTodos(pedido.drainDomainEvents());

        return PedidoApplicationMapper.toResponse(pedido);
    }
}

