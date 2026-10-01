package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.out.persistence.adapter;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.port.output.InventoryReferenceOwnershipPort;
import com.SITFAI_CORE_ERP_TIENDA.catalog.infrastructure.adapter.out.persistence.repository.ProductoJpaRepository;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.infrastructure.adapter.out.persistence.repository.SucursalJpaRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class InventoryReferenceOwnershipJpaAdapter implements InventoryReferenceOwnershipPort {

    private final SucursalJpaRepository sucursalRepository;
    private final ProductoJpaRepository productoRepository;

    public InventoryReferenceOwnershipJpaAdapter(
            SucursalJpaRepository sucursalRepository,
            ProductoJpaRepository productoRepository) {
        this.sucursalRepository = sucursalRepository;
        this.productoRepository = productoRepository;
    }

    @Override
    public boolean sucursalPerteneceAEmpresa(UUID sucursalId, UUID empresaId) {
        return sucursalRepository.existsByIdAndEmpresa_Id(sucursalId.toString(), empresaId.toString());
    }

    @Override
    public boolean productoPerteneceAEmpresa(UUID productoId, UUID empresaId) {
        return productoRepository.existsByIdAndEmpresaId(productoId.toString(), empresaId.toString());
    }
}
