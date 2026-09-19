package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.service;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.port.input.ListarBodegasCQRSUseCase;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.port.output.query.InventoryQueryRepository;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.query.dto.BodegaView;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.port.output.TenantProviderPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@Transactional(readOnly = true)
public class ListarBodegasCQRSService implements ListarBodegasCQRSUseCase {

    private final InventoryQueryRepository queryRepository;
    public ListarBodegasCQRSService(InventoryQueryRepository queryRepository) {
        this.queryRepository = Objects.requireNonNull(queryRepository, "queryRepository no puede ser null");
    }

    @Override
    public List<BodegaView> listarBodegasPorEmpresa(java.util.UUID empresaId) {
        return queryRepository.findBodegasByEmpresa(empresaId.toString());
    }
}
