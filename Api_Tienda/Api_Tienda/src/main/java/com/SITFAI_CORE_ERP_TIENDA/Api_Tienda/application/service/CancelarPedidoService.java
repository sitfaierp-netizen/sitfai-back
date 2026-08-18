package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.service;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.dto.CancelarPedidoCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.dto.PedidoResponse;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.mapper.PedidoApplicationMapper;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.port.input.CancelarPedidoUseCase;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.port.output.PedidoRepository;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.exception.PedidoNoEncontradoException;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.Pedido;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.PedidoId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * Caso de Uso: Cancelación de un Pedido indicando el motivo.
 * <p>
 * Aislamiento multi-inquilino estricto (MT-01, MT-02).
 */
@Service
@Transactional
public class CancelarPedidoService implements CancelarPedidoUseCase {

    private final PedidoRepository pedidoRepository;

    public CancelarPedidoService(PedidoRepository pedidoRepository) {
        this.pedidoRepository = Objects.requireNonNull(pedidoRepository, "pedidoRepository es requerido.");
    }

    @Override
    public PedidoResponse ejecutar(CancelarPedidoCommand command) {
        Objects.requireNonNull(command, "CancelarPedidoCommand no puede ser null.");

        EmpresaId empresaId = new EmpresaId(command.empresaId());
        PedidoId pedidoId = new PedidoId(command.pedidoId());

        // 1. Recuperación con validación de Tenant (MT-01, MT-02)
        Pedido pedido = pedidoRepository.buscarPorId(pedidoId, empresaId)
                .orElseThrow(() -> new PedidoNoEncontradoException(pedidoId, empresaId));

        // 2. Delegar comportamiento al Dominio (REGLA 1)
        pedido.cancelar(command.motivo());

        // 3. Persistir el agregado actualizado
        Pedido pedidoCancelado = pedidoRepository.guardar(pedido);

        // 4. Retornar DTO de respuesta
        return PedidoApplicationMapper.toResponse(pedidoCancelado);
    }
}
