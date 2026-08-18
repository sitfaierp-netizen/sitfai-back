package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.service;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.dto.AgregarLineaCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.dto.AgregarLineaPedidoCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.dto.PedidoResponse;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.dto.RemoverLineaPedidoCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.mapper.PedidoApplicationMapper;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.port.input.AgregarLineaPedidoUseCase;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.port.input.GestionarLineasPedidoUseCase;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.port.output.PedidoRepository;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.exception.PedidoNoEncontradoException;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.Pedido;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.Cantidad;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.Dinero;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.PedidoId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.ProductoId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * Caso de Uso: Gestión de líneas y productos en el pedido (agregar y remover).
 * <p>
 * Aislamiento multi-inquilino estricto (MT-01, MT-02).
 */
@Service
@Transactional
public class GestionarLineasPedidoService implements GestionarLineasPedidoUseCase, AgregarLineaPedidoUseCase {

    private final PedidoRepository pedidoRepository;

    public GestionarLineasPedidoService(PedidoRepository pedidoRepository) {
        this.pedidoRepository = Objects.requireNonNull(pedidoRepository, "pedidoRepository es requerido.");
    }

    @Override
    public PedidoResponse agregarLinea(AgregarLineaPedidoCommand command) {
        Objects.requireNonNull(command, "AgregarLineaPedidoCommand no puede ser null.");

        EmpresaId empresaId = new EmpresaId(command.empresaId());
        PedidoId pedidoId = new PedidoId(command.pedidoId());

        Pedido pedido = pedidoRepository.buscarPorId(pedidoId, empresaId)
                .orElseThrow(() -> new PedidoNoEncontradoException(pedidoId, empresaId));

        ProductoId productoId = new ProductoId(command.productoId());
        Cantidad cantidad = Cantidad.de(command.cantidad());
        Dinero precioUnitario = Dinero.de(command.precioUnitario(), command.moneda());

        // Delegación de invariante al Dominio (REGLA 1, REGLA 3)
        pedido.agregarItem(productoId, cantidad, precioUnitario);

        Pedido pedidoActualizado = pedidoRepository.guardar(pedido);
        return PedidoApplicationMapper.toResponse(pedidoActualizado);
    }

    @Override
    public PedidoResponse removerLinea(RemoverLineaPedidoCommand command) {
        Objects.requireNonNull(command, "RemoverLineaPedidoCommand no puede ser null.");

        EmpresaId empresaId = new EmpresaId(command.empresaId());
        PedidoId pedidoId = new PedidoId(command.pedidoId());

        Pedido pedido = pedidoRepository.buscarPorId(pedidoId, empresaId)
                .orElseThrow(() -> new PedidoNoEncontradoException(pedidoId, empresaId));

        // Delegación al Dominio
        pedido.removerItem(command.lineaId());

        Pedido pedidoActualizado = pedidoRepository.guardar(pedido);
        return PedidoApplicationMapper.toResponse(pedidoActualizado);
    }

    @Override
    public PedidoResponse ejecutar(AgregarLineaCommand command) {
        Objects.requireNonNull(command, "AgregarLineaCommand no puede ser null.");
        return agregarLinea(new AgregarLineaPedidoCommand(
                command.empresaId(),
                command.pedidoId(),
                command.productoId(),
                command.cantidad(),
                command.precioUnitario(),
                command.moneda()
        ));
    }
}
