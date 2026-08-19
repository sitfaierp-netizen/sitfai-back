package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.mapper;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.Bodega;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.MovimientoInventario;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.TipoBodega;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.StockLote;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.BodegaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.Cantidad;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.DocumentoFuenteId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.LoteId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.ProductoId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.PuntoReorden;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.SucursalId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.out.persistence.entity.BodegaJpaEntity;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.out.persistence.entity.MovimientoInventarioJpaEntity;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.out.persistence.entity.StockLoteJpaEntity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Mapper de Persistencia: Transforma entre Agregados/Entidades de Dominio y Entidades JPA.
 * <p>
 * Pertenece exclusivamente a la Capa de Infraestructura (REGLA-1, REGLA-2).
 * Garantiza que el Dominio permanezca completamente libre de dependencias JPA/Hibernate.
 */
@Component
public class BodegaPersistenceMapper {

    /**
     * Convierte una entidad JPA {@link BodegaJpaEntity} al Agregado {@link Bodega} del Dominio.
     */
    public Bodega toDomain(BodegaJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        // Mapear lotes
        List<StockLote> lotesDominio = new ArrayList<>();
        if (entity.getLotes() != null) {
            for (StockLoteJpaEntity loteJpa : entity.getLotes()) {
                lotesDominio.add(new StockLote(
                        new ProductoId(loteJpa.getProductoId()),
                        LoteId.de(loteJpa.getLoteId()),
                        loteJpa.getCantidad(),
                        loteJpa.getFechaCaducidad()
                ));
            }
        }

        // Mapear puntos de reorden
        Map<ProductoId, PuntoReorden> puntosReordenDominio = new HashMap<>();
        if (entity.getPuntosReorden() != null) {
            for (Map.Entry<UUID, BigDecimal> entry : entity.getPuntosReorden().entrySet()) {
                puntosReordenDominio.put(new ProductoId(entry.getKey()), PuntoReorden.de(entry.getValue()));
            }
        }

        // Mapear movimientos
        List<MovimientoInventario> movimientosDominio = new ArrayList<>();
        if (entity.getMovimientos() != null) {
            movimientosDominio = entity.getMovimientos().stream()
                    .map(this::toDomainMovimiento)
                    .collect(Collectors.toCollection(ArrayList::new));
        }

        return Bodega.reconstituir(
                new BodegaId(entity.getId()),
                new EmpresaId(entity.getEmpresaId()),
                new SucursalId(entity.getSucursalId()),
                entity.getCodigo(),
                entity.getNombre(),
                entity.isActiva(),
                entity.getTipo() != null ? TipoBodega.valueOf(entity.getTipo()) : TipoBodega.VENTA,
                lotesDominio,
                puntosReordenDominio,
                movimientosDominio,
                entity.getCreadoEn(),
                entity.getActualizadoEn(),
                entity.getVersion(),
                entity.isActivo(),
                entity.getDeletedAt(),
                entity.getDeletedBy()
        );
    }

    /**
     * Convierte un Agregado {@link Bodega} del Dominio a una entidad JPA {@link BodegaJpaEntity}.
     */
    public BodegaJpaEntity toJpaEntity(Bodega domain) {
        if (domain == null) {
            return null;
        }

        BodegaJpaEntity entity = new BodegaJpaEntity();
        entity.setId(domain.getId().valor());
        entity.setEmpresaId(domain.getEmpresaId().valor());
        entity.setSucursalId(domain.getSucursalId().valor());
        entity.setCodigo(domain.getCodigo());
        entity.setNombre(domain.getNombre());
        entity.setActiva(domain.isActiva());
        entity.setTipo(domain.getTipo() != null ? domain.getTipo().name() : TipoBodega.VENTA.name());
        entity.setCreadoEn(domain.getCreadoEn());
        entity.setActualizadoEn(domain.getActualizadoEn());
        entity.setVersion(domain.getVersion());
        entity.setActivo(domain.isActivo());
        entity.setDeletedAt(domain.getDeletedAt());
        entity.setDeletedBy(domain.getDeletedBy());

        // Mapear lotes
        List<StockLoteJpaEntity> lotesJpa = new ArrayList<>();
        if (domain.getLotes() != null) {
            for (StockLote loteDominio : domain.getLotes()) {
                UUID loteDbId = UUID.nameUUIDFromBytes(
                        (domain.getId().valor().toString() + "-" + loteDominio.getProductoId().valor().toString() + "-" + loteDominio.getLoteId().valor()).getBytes()
                );
                StockLoteJpaEntity loteJpa = new StockLoteJpaEntity(
                        loteDbId,
                        entity,
                        domain.getEmpresaId().valor(),
                        loteDominio.getProductoId().valor(),
                        loteDominio.getLoteId().valor(),
                        loteDominio.getCantidad(),
                        loteDominio.getFechaCaducidad()
                );
                lotesJpa.add(loteJpa);
            }
        }
        entity.setLotes(lotesJpa);

        // Mapear puntos de reorden
        Map<UUID, BigDecimal> puntosReordenJpa = new HashMap<>();
        if (domain.getPuntosReorden() != null) { // Asumiendo que se agregará getPuntosReorden en Bodega
            for (Map.Entry<ProductoId, PuntoReorden> entry : domain.getPuntosReorden().entrySet()) {
                puntosReordenJpa.put(entry.getKey().valor(), entry.getValue().valor());
            }
        }

        entity.setPuntosReorden(puntosReordenJpa);

        // Mapear movimientos con referencia bidireccional
        List<MovimientoInventarioJpaEntity> movimientosJpa = new ArrayList<>();
        if (domain.getMovimientos() != null) {
            for (MovimientoInventario mov : domain.getMovimientos()) {
                movimientosJpa.add(toJpaMovimiento(mov, entity));
            }
        }
        entity.setMovimientos(movimientosJpa);

        return entity;
    }

    /**
     * Convierte una entidad JPA {@link MovimientoInventarioJpaEntity} a la Entidad {@link MovimientoInventario} del Dominio.
     */
    public MovimientoInventario toDomainMovimiento(MovimientoInventarioJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return MovimientoInventario.reconstituir(
                entity.getId(),
                new BodegaId(entity.getBodega().getId()),
                new ProductoId(entity.getProductoId()),
                new EmpresaId(entity.getEmpresaId()),
                Cantidad.de(entity.getCantidad()),
                entity.getTipo(),
                entity.getLoteId() != null ? LoteId.de(entity.getLoteId()) : null,
                new DocumentoFuenteId(entity.getDocFuenteTipo(), entity.getDocFuenteNumero()),
                entity.getFechaRegistro()
        );
    }

    /**
     * Convierte una Entidad {@link MovimientoInventario} del Dominio a una entidad JPA {@link MovimientoInventarioJpaEntity}.
     */
    public MovimientoInventarioJpaEntity toJpaMovimiento(MovimientoInventario domain, BodegaJpaEntity bodegaEntity) {
        if (domain == null) {
            return null;
        }

        return new MovimientoInventarioJpaEntity(
                domain.getId(),
                bodegaEntity,
                domain.getProductoId().valor(),
                domain.getEmpresaId().valor(),
                domain.getCantidad().valor(),
                domain.getTipo(),
                domain.getLoteId() != null ? domain.getLoteId().valor() : null,
                domain.getDocumentoFuente().tipo(),
                domain.getDocumentoFuente().numero(),
                domain.getFechaRegistro()
        );
    }
}
