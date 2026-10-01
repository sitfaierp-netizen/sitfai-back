package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.service;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.BodegaResponse;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.Bodega;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.port.input.ConsultarBodegasPorSucursalUseCase;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.port.output.BodegaRepository;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.EmpresaId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class ConsultarBodegasService implements ConsultarBodegasPorSucursalUseCase {

    private final BodegaRepository bodegaRepository;

    public ConsultarBodegasService(BodegaRepository bodegaRepository) {
        this.bodegaRepository = Objects.requireNonNull(bodegaRepository);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BodegaResponse> listarPorSucursal(String empresaId, String sucursalId) {
        Objects.requireNonNull(empresaId, "empresaId es requerido");
        Objects.requireNonNull(sucursalId, "sucursalId es requerido");

        List<Bodega> bodegas = bodegaRepository.listarPorSucursal(EmpresaId.de(empresaId), sucursalId);

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
