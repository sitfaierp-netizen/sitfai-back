package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.service;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.dto.CrearPedidoCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.dto.PedidoResponse;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.mapper.PedidoApplicationMapper;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.port.input.CrearBorradorPedidoUseCase;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.port.input.CrearPedidoUseCase;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.port.output.PedidoRepository;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.Pedido;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.ClienteId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.EmpresaId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * Caso de Uso: Inicializar un nuevo Pedido en estado CREADO.
 * <p>
 * Aislamiento multi-inquilino estricto mediante {@link EmpresaId} (MT-01).
 */
@Service
@Transactional
public class CrearPedidoService implements CrearPedidoUseCase, CrearBorradorPedidoUseCase {

    private final PedidoRepository pedidoRepository;

    public CrearPedidoService(PedidoRepository pedidoRepository) {
        this.pedidoRepository = Objects.requireNonNull(pedidoRepository, "CrearPedidoService: pedidoRepository no puede ser null.");
    }

    @Override
    public PedidoResponse ejecutar(CrearPedidoCommand command) {
        Objects.requireNonNull(command, "CrearPedidoService: command no puede ser null.");

        EmpresaId empresaId = new EmpresaId(command.empresaId());
        ClienteId clienteId = new ClienteId(command.clienteId());

        // Delegación al factory method del Dominio
        Pedido nuevoPedido = Pedido.iniciar(empresaId, clienteId);

        // Persistencia a través del Driven Port
        Pedido pedidoGuardado = pedidoRepository.guardar(nuevoPedido);

        return PedidoApplicationMapper.toResponse(pedidoGuardado);
    }
}
