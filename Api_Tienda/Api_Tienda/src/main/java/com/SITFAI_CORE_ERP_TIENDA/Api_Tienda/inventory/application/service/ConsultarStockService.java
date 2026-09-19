package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.service;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.port.input.ConsultarStockUseCase;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.port.output.TenantProviderPort;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.port.output.query.InventoryQueryRepository;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.query.dto.MovimientoKardexView;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.query.dto.StockDisponibleView;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class ConsultarStockService implements ConsultarStockUseCase {

    private final InventoryQueryRepository queryRepository;
    private final TenantProviderPort tenantProvider;

    public ConsultarStockService(InventoryQueryRepository queryRepository, TenantProviderPort tenantProvider) {
        this.queryRepository = Objects.requireNonNull(queryRepository);
        this.tenantProvider = Objects.requireNonNull(tenantProvider);
    }

    @Override
    public List<StockDisponibleView> consultarStockBodega(String bodegaId) {
        // MT-02: Se obtiene el tenant de manera segura desde el contexto
        String empresaId = tenantProvider.getEmpresaIdAutenticada().valor().toString();
        return queryRepository.findStockByEmpresaAndBodega(empresaId, bodegaId);
    }

    @Override
    public List<MovimientoKardexView> consultarKardex(String bodegaId, String productoId) {
        // MT-02: Se obtiene el tenant de manera segura desde el contexto
        String empresaId = tenantProvider.getEmpresaIdAutenticada().valor().toString();
        return queryRepository.findKardexByEmpresaBodegaAndProducto(empresaId, bodegaId, productoId);
    }
}
