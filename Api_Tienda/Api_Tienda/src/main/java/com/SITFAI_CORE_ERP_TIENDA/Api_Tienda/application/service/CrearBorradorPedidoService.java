package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.service;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.dto.CrearPedidoCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.dto.PedidoResponse;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.mapper.PedidoApplicationMapper;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.port.input.CrearBorradorPedidoUseCase;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.port.output.PedidoRepository;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.Pedido;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.ClienteId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.EmpresaId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * Application Service: Inicialización de borradores de pedidos.
 * <p>
 * Orquesta la creación del agregado {@link Pedido} y su persistencia inicial.
 */
@Service
public class CrearBorradorPedidoService implements CrearBorradorPedidoUseCase {

    private final PedidoRepository pedidoRepository;

    public CrearBorradorPedidoService(PedidoRepository pedidoRepository) {
        this.pedidoRepository = Objects.requireNonNull(pedidoRepository, "pedidoRepository es requerido.");
    }

    @Override
    @Transactional
    public PedidoResponse ejecutar(CrearPedidoCommand command) {
        Objects.requireNonNull(command, "CrearPedidoCommand no puede ser null.");

        EmpresaId empresaId = EmpresaId.de(command.empresaId());
        ClienteId clienteId = ClienteId.de(command.clienteId());

        Pedido nuevoPedido = Pedido.crear(empresaId, clienteId);
        Pedido pedidoGuardado = pedidoRepository.guardar(nuevoPedido);

        return PedidoApplicationMapper.toResponse(pedidoGuardado);
    }
}
