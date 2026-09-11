package com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.port.output;

import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.OrdenCompra;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.vo.OrdenCompraId;

import java.util.Optional;
import java.util.UUID;

public interface OrdenCompraRepository {

    OrdenCompra guardar(OrdenCompra ordenCompra);

    Optional<OrdenCompra> buscarPorIdYEmpresaId(OrdenCompraId id, UUID empresaId);

    org.springframework.data.domain.Page<OrdenCompra> listarOrdenes(UUID empresaId, org.springframework.data.domain.Pageable pageable);

}
