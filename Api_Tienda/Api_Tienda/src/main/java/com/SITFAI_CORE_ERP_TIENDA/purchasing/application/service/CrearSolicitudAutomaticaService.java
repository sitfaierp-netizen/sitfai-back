package com.SITFAI_CORE_ERP_TIENDA.purchasing.application.service;

import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.dto.CrearSolicitudAutomaticaCommand;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.dto.SolicitudResponse;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.port.input.CrearSolicitudAutomaticaUseCase;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.port.output.SolicitudAbastecimientoRepository;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.LineaSolicitud;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.SolicitudAbastecimiento;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.valueobject.BodegaId;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.valueobject.ProductoId;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.valueobject.SolicitudId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * Servicio de Aplicación: Orquesta la creación automática de una Solicitud de Abastecimiento.
 * <p>
 * Este servicio se activa por el módulo {@code replenishment} vía Domain Event asíncrono.
 * La solicitud se crea en estado {@code BORRADOR} (NO en PENDIENTE_APROBACION)
 * para que el equipo de Compras la enriquezca con proveedor y costos antes de emitirla.
 * <p>
 * Regla REGLA-1: Este servicio NO contiene lógica de negocio — la delega al Agregado.
 * Regla MT-01: El tenant (empresaId) proviene del comando originado en el JWT, nunca del caller.
 * Regla Protocolo-5 (MCP): Consumidor asíncrono de Domain Events inter-módulos.
 */
@Service
public class CrearSolicitudAutomaticaService implements CrearSolicitudAutomaticaUseCase {

    private static final Logger log = LoggerFactory.getLogger(CrearSolicitudAutomaticaService.class);

    private final SolicitudAbastecimientoRepository repository;

    public CrearSolicitudAutomaticaService(SolicitudAbastecimientoRepository repository) {
        this.repository = Objects.requireNonNull(repository, "SolicitudAbastecimientoRepository es obligatorio.");
    }

    @Override
    @Transactional
    public SolicitudResponse crearAutomatica(CrearSolicitudAutomaticaCommand command) {
        log.info("Purchasing: Creando SolicitudAbastecimiento automática. empresa={}, bodega={}, producto={}, cantidad={}",
                command.empresaId(), command.bodegaId(), command.productoId(), command.cantidadRequerida());

        // 1. Instanciar el Agregado en estado BORRADOR (vía factory method del dominio)
        SolicitudAbastecimiento solicitud = SolicitudAbastecimiento.iniciar(
                SolicitudId.generar(),
                new EmpresaId(command.empresaId()),
                new BodegaId(command.bodegaId())
        );

        // 2. Agregar la línea del producto que necesita reposición
        //    La lógica de validación de cantidad vive en el Dominio (LineaSolicitud.crear)
        solicitud.agregarLinea(
                LineaSolicitud.crear(
                        new ProductoId(command.productoId()),
                        command.cantidadRequerida()
                )
        );

        // NOTA DELIBERADA DE DISEÑO: La solicitud se DEJA en estado BORRADOR.
        // El área de Compras debe asignarle proveedor y costo antes de enviarla a aprobación.
        // Si se llamara a solicitud.solicitarAprobacion() aquí, el flujo sería totalmente
        // automático sin revisión humana — violación de BP-04 (Four-Eyes Principle).

        // 3. Persistir
        repository.guardar(solicitud);

        log.info("Purchasing: SolicitudAbastecimiento automática creada exitosamente. solicitudId={}, estado={}",
                solicitud.getId().valor(), solicitud.getEstado().name());

        // 4. Mapear a DTO de respuesta
        return toResponse(solicitud);
    }

    // -------------------------------------------------------------------------
    // Mapper privado: Dominio → DTO de respuesta
    // -------------------------------------------------------------------------
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
