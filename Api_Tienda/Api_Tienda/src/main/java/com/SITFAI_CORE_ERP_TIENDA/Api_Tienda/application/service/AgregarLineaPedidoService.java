package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.service;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.dto.AgregarLineaCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.dto.PedidoResponse;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.mapper.PedidoApplicationMapper;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.port.input.AgregarLineaPedidoUseCase;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.port.output.PedidoRepository;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.exception.PedidoNoEncontradoException;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.Pedido;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.Dinero;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.PedidoId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.ProductoId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * Application Service: Adición de ítems a un pedido en borrador.
 * <p>
 * Orquesta la búsqueda multitenant, delegando la lógica de agrupación e incremento al dominio.
 */
@Service
public class AgregarLineaPedidoService implements AgregarLineaPedidoUseCase {

    private final PedidoRepository pedidoRepository;

    public AgregarLineaPedidoService(PedidoRepository pedidoRepository) {
        this.pedidoRepository = Objects.requireNonNull(pedidoRepository, "pedidoRepository es requerido.");
    }

    @Override
    @Transactional
    public PedidoResponse ejecutar(AgregarLineaCommand command) {
        Objects.requireNonNull(command, "AgregarLineaCommand no puede ser null.");

        EmpresaId empresaId = EmpresaId.de(command.empresaId());
        PedidoId pedidoId = PedidoId.de(command.pedidoId());
        ProductoId productoId = ProductoId.de(command.productoId());
        Dinero precioUnitario = Dinero.de(command.precioUnitario(), command.moneda());

        Pedido pedido = pedidoRepository.buscarPorId(pedidoId, empresaId)
                .orElseThrow(() -> new PedidoNoEncontradoException(pedidoId, empresaId));

        // Delegar lógica e invariantes al agregado
        pedido.agregarLinea(productoId, command.cantidad(), precioUnitario);

        Pedido pedidoActualizado = pedidoRepository.guardar(pedido);



        return PedidoApplicationMapper.toResponse(pedidoActualizado);
    }
}
