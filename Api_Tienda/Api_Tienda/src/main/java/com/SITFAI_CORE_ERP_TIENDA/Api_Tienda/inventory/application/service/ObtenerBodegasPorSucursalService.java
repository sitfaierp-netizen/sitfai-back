package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.service;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.BodegaResponse;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.Bodega;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.port.input.ObtenerBodegasPorSucursalUseCase;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.port.output.BodegaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.port.output.InventoryReferenceOwnershipPort;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.exception.InventoryReferenceNotFoundException;

@Service
public class ObtenerBodegasPorSucursalService implements ObtenerBodegasPorSucursalUseCase {

    private final BodegaRepository bodegaRepository;
    private final InventoryReferenceOwnershipPort referenceOwnershipPort;

    public ObtenerBodegasPorSucursalService(
            BodegaRepository bodegaRepository,
            InventoryReferenceOwnershipPort referenceOwnershipPort) {
        this.bodegaRepository = Objects.requireNonNull(bodegaRepository);
        this.referenceOwnershipPort = Objects.requireNonNull(referenceOwnershipPort);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BodegaResponse> ejecutar(String empresaId, String sucursalId) {
        Objects.requireNonNull(empresaId, "empresaId es requerido");
        Objects.requireNonNull(sucursalId, "sucursalId es requerido");

        EmpresaId tenant = EmpresaId.de(empresaId);
        UUID sucursalUuid = UUID.fromString(sucursalId);
        if (!referenceOwnershipPort.sucursalPerteneceAEmpresa(sucursalUuid, tenant.valor())) {
            throw new InventoryReferenceNotFoundException();
        }

        List<Bodega> bodegas = bodegaRepository.listarPorSucursal(tenant, sucursalId);

        return bodegas.stream().map(bodega -> new BodegaResponse(
                bodega.getId().valor().toString(),
                bodega.getEmpresaId().valor().toString(),
                bodega.getSucursalId().valor().toString(),
                bodega.getCodigo(),
                bodega.getNombre(),
                bodega.isActiva(),
                bodega.getCreadoEn()
        )).toList();
    }
}
