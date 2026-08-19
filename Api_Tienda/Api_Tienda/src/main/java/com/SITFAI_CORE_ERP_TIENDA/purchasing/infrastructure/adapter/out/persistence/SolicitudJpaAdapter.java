package com.SITFAI_CORE_ERP_TIENDA.purchasing.infrastructure.adapter.out.persistence;

import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.port.output.SolicitudAbastecimientoRepository;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.LineaSolicitud;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.SolicitudAbastecimiento;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.valueobject.*;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.infrastructure.adapter.out.persistence.entity.LineaSolicitudJpaEntity;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.infrastructure.adapter.out.persistence.entity.SolicitudJpaEntity;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.infrastructure.adapter.out.persistence.repository.SolicitudJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Driven Adapter: Implementa SolicitudAbastecimientoRepository usando JPA.
 * Traduce Dominio ↔ JPA Entity. El Dominio nunca ve esta clase.
 */
@Repository
public class SolicitudJpaAdapter implements SolicitudAbastecimientoRepository {

    private final SolicitudJpaRepository jpaRepository;

    public SolicitudJpaAdapter(SolicitudJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void guardar(SolicitudAbastecimiento solicitud) {
        SolicitudJpaEntity entity = toEntity(solicitud);
        jpaRepository.save(entity);
    }

    @Override
    public Optional<SolicitudAbastecimiento> buscarPorIdYEmpresa(SolicitudId id, EmpresaId empresaId) {
        return jpaRepository
                .findByIdAndEmpresaId(id.toString(), empresaId.valor().toString())
                .map(this::toDomain);
    }

    // -------------------------------------------------------------------------
    // Mappers privados: Dominio → JPA Entity
    // -------------------------------------------------------------------------
    private SolicitudJpaEntity toEntity(SolicitudAbastecimiento s) {
        SolicitudJpaEntity entity = new SolicitudJpaEntity(
                s.getId().toString(),
                s.getEmpresaId().valor().toString(),
                s.getBodegaId().valor().toString(),
                s.getEstado().name(),
                s.getCreadoEn()
        );

        s.getLineas().forEach(linea -> {
            LineaSolicitudJpaEntity lineaEntity = new LineaSolicitudJpaEntity(
                    UUID.randomUUID().toString(),
                    entity,
                    s.getEmpresaId().valor().toString(),
                    linea.getProductoId().valor().toString(),
                    linea.getCantidadSolicitada()
            );
            entity.getLineas().add(lineaEntity);
        });

        return entity;
    }

    // -------------------------------------------------------------------------
    // Mappers privados: JPA Entity → Dominio
    // -------------------------------------------------------------------------
    private SolicitudAbastecimiento toDomain(SolicitudJpaEntity e) {
        List<LineaSolicitud> lineas = e.getLineas().stream()
                .map(l -> LineaSolicitud.crear(
                        new ProductoId(UUID.fromString(l.getProductoId())),
                        l.getCantidadSolicitada()))
                .toList();

        return SolicitudAbastecimiento.reconstituir(
                SolicitudId.de(e.getId()),
                new EmpresaId(UUID.fromString(e.getEmpresaId())),
                new BodegaId(UUID.fromString(e.getBodegaId())),
                EstadoSolicitud.valueOf(e.getEstado()),
                lineas,
                e.getCreadoEn()
        );
    }
}
