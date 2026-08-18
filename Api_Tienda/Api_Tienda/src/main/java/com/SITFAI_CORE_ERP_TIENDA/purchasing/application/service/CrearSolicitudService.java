package com.SITFAI_CORE_ERP_TIENDA.purchasing.application.service;

import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.dto.CrearSolicitudCommand;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.dto.SolicitudResponse;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.port.input.CrearSolicitudUseCase;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.port.output.SolicitudAbastecimientoRepository;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.LineaSolicitud;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.SolicitudAbastecimiento;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.valueobject.BodegaId;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.valueobject.ProductoId;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.valueobject.SolicitudId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * Application Service: Orquesta la creación de una SolicitudAbastecimiento.
 * No contiene lógica de negocio — la delega al Agregado (Regla 1).
 */
@Service
@Transactional
public class CrearSolicitudService implements CrearSolicitudUseCase {

    private final SolicitudAbastecimientoRepository repository;

    public CrearSolicitudService(SolicitudAbastecimientoRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    @Override
    public SolicitudResponse crear(CrearSolicitudCommand command) {
        // 1. Instanciar el Agregado mediante su factory method
        SolicitudAbastecimiento solicitud = SolicitudAbastecimiento.iniciar(
                SolicitudId.generar(),
                new EmpresaId(command.empresaId()),
                new BodegaId(command.bodegaId())
        );

        // 2. Agregar líneas al Agregado (la lógica de validación vive en el Dominio)
        command.lineas().forEach(lineaCmd ->
                solicitud.agregarLinea(LineaSolicitud.crear(
                        new ProductoId(lineaCmd.productoId()),
                        lineaCmd.cantidadSolicitada()
                ))
        );

        // 3. Enviar a aprobación (valida que no esté vacía — invariante de dominio)
        solicitud.solicitarAprobacion();

        // 4. Persistir
        repository.guardar(solicitud);

        // 5. Mapear a DTO de respuesta
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
