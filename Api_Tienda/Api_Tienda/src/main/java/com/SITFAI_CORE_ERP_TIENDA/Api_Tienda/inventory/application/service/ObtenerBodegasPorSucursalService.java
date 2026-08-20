package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.service;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.BodegaResponse;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.Bodega;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.port.input.ObtenerBodegasPorSucursalUseCase;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.port.output.BodegaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class ObtenerBodegasPorSucursalService implements ObtenerBodegasPorSucursalUseCase {

    private final BodegaRepository bodegaRepository;

    public ObtenerBodegasPorSucursalService(BodegaRepository bodegaRepository) {
        this.bodegaRepository = Objects.requireNonNull(bodegaRepository);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BodegaResponse> ejecutar(String sucursalId) {
        Objects.requireNonNull(sucursalId, "sucursalId es requerido");

        List<Bodega> bodegas = bodegaRepository.listarPorSucursalId(sucursalId);

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
