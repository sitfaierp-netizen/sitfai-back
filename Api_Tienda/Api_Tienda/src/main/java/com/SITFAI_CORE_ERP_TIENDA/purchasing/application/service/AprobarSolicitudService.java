package com.SITFAI_CORE_ERP_TIENDA.purchasing.application.service;

import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.dto.AprobarSolicitudCommand;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.dto.SolicitudResponse;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.port.input.AprobarSolicitudUseCase;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.port.output.SolicitudAbastecimientoRepository;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.SolicitudAbastecimiento;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.valueobject.SolicitudId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * Application Service: Orquesta la aprobación de una SolicitudAbastecimiento.
 * El EmpresaId proviene del token JWT — garantía MT-01.
 */
@Service
@Transactional
public class AprobarSolicitudService implements AprobarSolicitudUseCase {

    private final SolicitudAbastecimientoRepository repository;

    public AprobarSolicitudService(SolicitudAbastecimientoRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    @Override
    public SolicitudResponse aprobar(AprobarSolicitudCommand command) {
        // 1. Recuperar el Agregado filtrando por tenant (MT-01)
        SolicitudId solicitudId = SolicitudId.de(command.solicitudId().toString());
        EmpresaId empresaId     = new EmpresaId(command.empresaId());

        SolicitudAbastecimiento solicitud = repository
                .buscarPorIdYEmpresa(solicitudId, empresaId)
                .orElseThrow(() -> new com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.exception.DomainException(
                        "SolicitudAbastecimiento no encontrada o no pertenece al tenant: " + solicitudId));

        // 2. Delegar la transición al Agregado (lógica en el Dominio)
        solicitud.aprobar();

        // 3. Persistir
        repository.guardar(solicitud);

        // 4. Mapear y devolver
        return toResponse(solicitud);
    }

    private SolicitudResponse toResponse(SolicitudAbastecimiento s) {
        var lineas = s.getLineas().stream()
                .map(l -> new SolicitudResponse.LineaSolicitudResponse(
                        l.getProductoId().valor(),
                        l.getCantidadSolicitada()))
                .toList();

        return new SolicitudResponse(
                s.getId().valor(),
                s.getEmpresaId().valor(),
                s.getBodegaId().valor(),
                s.getEstado().name(),
                s.getCreadoEn(),
                lineas
        );
    }
}
