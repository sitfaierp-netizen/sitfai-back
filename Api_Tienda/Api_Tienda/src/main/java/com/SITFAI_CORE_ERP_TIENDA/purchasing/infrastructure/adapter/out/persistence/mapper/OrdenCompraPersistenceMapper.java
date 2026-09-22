package com.SITFAI_CORE_ERP_TIENDA.purchasing.infrastructure.adapter.out.persistence.mapper;

import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.ordencompra.LineaOrdenCompra;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.ordencompra.OrdenCompra;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.ordencompra.vo.*;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.infrastructure.adapter.out.persistence.entity.LineaOrdenCompraJpaEntity;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.infrastructure.adapter.out.persistence.entity.OrdenCompraJpaEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Mapper de Persistencia: Traduce bidireccionalmente entre el Agregado de Dominio {@link OrdenCompra}
 * y la entidad relacional {@link OrdenCompraJpaEntity}.
 * <p>
 * Regla 1 (Clean Architecture): Aísla el dominio de los detalles de infraestructura relacional.
 * Regla MT-01: Propaga inquebrantablemente el {@code empresa_id} en cabecera y líneas.
 */
public final class OrdenCompraPersistenceMapper {

    private OrdenCompraPersistenceMapper() {}

    public static OrdenCompraJpaEntity toEntity(OrdenCompra domain) {
        if (domain == null) return null;

        OrdenCompraJpaEntity entity = new OrdenCompraJpaEntity();
        entity.setId(domain.getId().valor().toString());
        entity.setEmpresaId(domain.getEmpresaId().valor().toString());
        entity.setProveedorId(domain.getProveedorId().valor().toString());
        entity.setEstado(domain.getEstado().name());
        entity.setCostoTotal(domain.calcularTotalEsperado());
        entity.setEmitidoEn(domain.getEmitidoEn());
        entity.setCreadoEn(domain.getCreadoEn());

        if (domain.getLineas() != null) {
            for (LineaOrdenCompra lineaDomain : domain.getLineas()) {
                LineaOrdenCompraJpaEntity lineaEntity = new LineaOrdenCompraJpaEntity(
                        UUID.randomUUID().toString(),
                        domain.getEmpresaId().valor().toString(),
                        lineaDomain.getProductoId().valor().toString(),
                        BigDecimal.valueOf(lineaDomain.getCantidadSolicitada()),
                        lineaDomain.getCostoUnitarioEsperado(),
                        lineaDomain.getSubtotalEsperado()
                );
                entity.addLinea(lineaEntity);
            }
        }

        return entity;
    }

    public static OrdenCompra toDomain(OrdenCompraJpaEntity entity) {
        if (entity == null) return null;

        List<LineaOrdenCompra> lineasDomain = entity.getLineas() != null
                ? entity.getLineas().stream()
                        .map(OrdenCompraPersistenceMapper::toLineaDomain)
                        .collect(Collectors.toList())
                : new ArrayList<>();

        Instant creadoEn = entity.getCreadoEn() != null ? entity.getCreadoEn() : Instant.now();

        return OrdenCompra.reconstituir(
                OrdenCompraId.de(entity.getId()),
                EmpresaId.de(entity.getEmpresaId()),
                ProveedorId.de(entity.getProveedorId()),
                EstadoOrdenCompra.valueOf(entity.getEstado()),
                lineasDomain,
                creadoEn,
                entity.getEmitidoEn()
        );
    }

    private static LineaOrdenCompra toLineaDomain(LineaOrdenCompraJpaEntity entity) {
        return LineaOrdenCompra.crear(
                ProductoId.de(entity.getProductoId()),
                entity.getCantidadSolicitada() != null ? entity.getCantidadSolicitada().intValue() : 0,
                entity.getCostoUnitarioEsperado() != null ? entity.getCostoUnitarioEsperado() : BigDecimal.ZERO
        );
    }
}
