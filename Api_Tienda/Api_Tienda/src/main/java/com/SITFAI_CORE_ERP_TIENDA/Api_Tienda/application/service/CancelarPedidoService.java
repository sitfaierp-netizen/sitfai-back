package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.service;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.dto.CancelarPedidoCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.dto.PedidoResponse;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.mapper.PedidoApplicationMapper;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.port.input.CancelarPedidoUseCase;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.port.output.PedidoEventPublisher;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.port.output.PedidoRepository;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.exception.PedidoNoEncontradoException;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.Pedido;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.PedidoId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@Transactional
public class CancelarPedidoService implements CancelarPedidoUseCase {

    private final PedidoRepository pedidoRepository;
    private final PedidoEventPublisher eventPublisher;

    public CancelarPedidoService(PedidoRepository pedidoRepository, PedidoEventPublisher eventPublisher) {
        this.pedidoRepository = Objects.requireNonNull(pedidoRepository);
        this.eventPublisher = Objects.requireNonNull(eventPublisher);
    }

    @Override
    public PedidoResponse ejecutar(CancelarPedidoCommand command) {
        PedidoId pedidoId = PedidoId.de(command.pedidoId());
        EmpresaId empresaId = EmpresaId.de(command.empresaId());

        Pedido pedido = pedidoRepository.buscarPorId(pedidoId, empresaId)
                .orElseThrow(() -> new PedidoNoEncontradoException(pedidoId, empresaId));

        // SAGA Compensation logic: cancelarPorFaltaDeStock si viene de la saga, o cancelar normal
        if (command.motivo() != null && command.motivo().startsWith("SAGA-COMPENSACION: ")) {
            String motivoLimpio = command.motivo().replace("SAGA-COMPENSACION: ", "");
            pedido.cancelarPorFaltaDeStock(motivoLimpio);
        } else {
            pedido.cancelar(command.motivo());
        }

        pedidoRepository.guardar(pedido);

        eventPublisher.publicarTodos(pedido.drainDomainEvents());

        return PedidoApplicationMapper.toResponse(pedido);
    }
}
