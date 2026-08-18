package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.service;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.dto.PedidoResponse;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.mapper.PedidoApplicationMapper;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.port.input.ConsultarPedidoUseCase;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.port.output.PedidoRepository;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.exception.PedidoNoEncontradoException;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.Pedido;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.PedidoId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Caso de Uso: Consulta de Pedidos por ID o por Empresa.
 * <p>
 * Aislamiento multi-inquilino estricto (MT-01, MT-02).
 */
@Service
@Transactional(readOnly = true)
public class ConsultarPedidoService implements ConsultarPedidoUseCase {

    private final PedidoRepository pedidoRepository;

    public ConsultarPedidoService(PedidoRepository pedidoRepository) {
        this.pedidoRepository = Objects.requireNonNull(pedidoRepository, "pedidoRepository es requerido.");
    }

    @Override
    public PedidoResponse porId(UUID pedidoId, UUID empresaId) {
        Objects.requireNonNull(pedidoId, "pedidoId es obligatorio.");
        Objects.requireNonNull(empresaId, "empresaId es obligatorio (MT-01).");

        PedidoId id = new PedidoId(pedidoId);
        EmpresaId empresa = new EmpresaId(empresaId);

        Pedido pedido = pedidoRepository.buscarPorId(id, empresa)
                .orElseThrow(() -> new PedidoNoEncontradoException(id, empresa));

        return PedidoApplicationMapper.toResponse(pedido);
    }

    @Override
    public List<PedidoResponse> porEmpresa(UUID empresaId) {
        Objects.requireNonNull(empresaId, "empresaId es obligatorio (MT-01).");

        EmpresaId empresa = new EmpresaId(empresaId);
        List<Pedido> pedidos = pedidoRepository.buscarPorEmpresa(empresa);

        return pedidos.stream()
                .map(PedidoApplicationMapper::toResponse)
                .toList();
    }
}
