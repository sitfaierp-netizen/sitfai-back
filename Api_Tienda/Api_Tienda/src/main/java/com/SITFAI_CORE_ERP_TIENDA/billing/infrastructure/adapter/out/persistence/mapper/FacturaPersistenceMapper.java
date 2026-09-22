package com.SITFAI_CORE_ERP_TIENDA.billing.infrastructure.adapter.out.persistence.mapper;

import com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.factura.Factura;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.factura.LineaFactura;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.factura.vo.ClienteId;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.factura.vo.DocumentoFuenteId;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.factura.vo.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.factura.vo.EstadoFactura;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.factura.vo.FacturaId;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.valueobject.Dinero;
import com.SITFAI_CORE_ERP_TIENDA.billing.infrastructure.adapter.out.persistence.entity.FacturaJpaEntity;
import com.SITFAI_CORE_ERP_TIENDA.billing.infrastructure.adapter.out.persistence.entity.LineaFacturaJpaEntity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Mapper de Persistencia: Traduce bidireccionalmente entre el Agregado de Dominio {@link Factura}
 * y la entidad relacional {@link FacturaJpaEntity}.
 * <p>
 * Regla 1 (Clean Architecture): Aísla el dominio de detalles técnicos de base de datos y JPA.
 * Regla MT-01: Propaga inquebrantablemente el {@code empresa_id} en cabecera y líneas.
 */
public final class FacturaPersistenceMapper {

    private FacturaPersistenceMapper() {}

    public static FacturaJpaEntity toEntity(Factura domain) {
        if (domain == null) return null;

        FacturaJpaEntity entity = new FacturaJpaEntity();
        entity.setId(domain.getId().valor());
        entity.setEmpresaId(domain.getEmpresaId().valor());
        entity.setClienteId(domain.getClienteId().valor());

        if (domain.getDocumentoFuenteId() != null) {
            entity.setTipoOrigen(domain.getDocumentoFuenteId().tipo());
            try {
                entity.setDocumentoFuenteId(UUID.fromString(domain.getDocumentoFuenteId().numero()));
            } catch (Exception ignored) {
                // Si el número de documento fuente no es UUID nativo (ej. string ticket POS)
            }
            if ("PEDIDO_ECOMMERCE".equalsIgnoreCase(domain.getDocumentoFuenteId().tipo()) ||
                "ECOMMERCE".equalsIgnoreCase(domain.getDocumentoFuenteId().tipo())) {
                try {
                    entity.setPedidoId(UUID.fromString(domain.getDocumentoFuenteId().numero()));
                } catch (Exception ignored) {}
            }
        }

        entity.setSubtotal(domain.getSubtotal().monto());
        entity.setTotalImpuestos(domain.getTotalImpuestos().monto());
        entity.setTotalGeneral(domain.getTotal().monto());
        entity.setEstado(domain.getEstado().name());
        entity.setMotivoAnulacion(domain.getMotivoAnulacion());
        entity.setAnuladoEn(domain.getAnuladoEn());

        if (domain.getLineas() != null) {
            for (LineaFactura lineaDomain : domain.getLineas()) {
                LineaFacturaJpaEntity lineaEntity = new LineaFacturaJpaEntity();
                lineaEntity.setId(lineaDomain.getId());
                lineaEntity.setEmpresaId(domain.getEmpresaId().valor());
                lineaEntity.setConcepto(lineaDomain.getDescripcion());
                lineaEntity.setCantidad(lineaDomain.getCantidad());
                lineaEntity.setPrecioUnitario(lineaDomain.getPrecioUnitario().monto());
                lineaEntity.setSubtotal(lineaDomain.calcularSubtotal().monto());
                lineaEntity.setTotalImpuestos(lineaDomain.calcularTotalImpuestos().monto());
                lineaEntity.setTotal(lineaDomain.calcularTotal().monto());
                entity.addLinea(lineaEntity);
            }
        }

        return entity;
    }

    public static Factura toDomain(FacturaJpaEntity entity) {
        if (entity == null) return null;

        List<LineaFactura> lineasDomain = entity.getLineas() != null
                ? entity.getLineas().stream()
                        .map(FacturaPersistenceMapper::toLineaDomain)
                        .collect(Collectors.toList())
                : new ArrayList<>();

        DocumentoFuenteId docFuente;
        if (entity.getTipoOrigen() != null && entity.getDocumentoFuenteId() != null) {
            docFuente = DocumentoFuenteId.de(entity.getTipoOrigen(), entity.getDocumentoFuenteId().toString());
        } else if (entity.getPedidoId() != null) {
            docFuente = DocumentoFuenteId.ecommerce(entity.getPedidoId().toString());
        } else {
            docFuente = DocumentoFuenteId.de("DIRECTA", entity.getId().toString());
        }

        Instant emitidoEn = entity.getCreadoEn() != null ? entity.getCreadoEn() : Instant.now();

        return Factura.reconstituir(
                FacturaId.de(entity.getId()),
                EmpresaId.de(entity.getEmpresaId()),
                docFuente,
                ClienteId.de(entity.getClienteId()),
                EstadoFactura.valueOf(entity.getEstado()),
                lineasDomain,
                Dinero.de(entity.getSubtotal()),
                Dinero.de(entity.getTotalImpuestos()),
                Dinero.de(entity.getTotalGeneral()),
                emitidoEn,
                entity.getAnuladoEn(),
                entity.getMotivoAnulacion()
        );
    }

    private static LineaFactura toLineaDomain(LineaFacturaJpaEntity entity) {
        return new LineaFactura(
                entity.getId(),
                entity.getConcepto(),
                entity.getCantidad(),
                Dinero.de(entity.getPrecioUnitario()),
                new ArrayList<>()
        );
    }

    // Métodos de compatibilidad con modelo legado Factura
    public static FacturaJpaEntity toEntity(com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.Factura domain) {
        if (domain == null) return null;
        FacturaJpaEntity entity = new FacturaJpaEntity();
        entity.setId(domain.getId().value());
        entity.setEmpresaId(domain.getEmpresaId());
        entity.setClienteId(domain.getClienteId().value());
        if (domain.getPedidoId() != null) {
            entity.setPedidoId(domain.getPedidoId().value());
        }
        entity.setRucCliente(domain.getRucCliente().valor());
        entity.setSubtotal(domain.getSubtotal().monto());
        entity.setTotalImpuestos(domain.getTotalImpuestos().monto());
        entity.setTotalGeneral(domain.getTotalGeneral().monto());
        entity.setEstado(domain.getEstado().name());
        entity.setVersion(domain.getVersion());
        entity.setCreadoEn(domain.getCreatedAt());
        entity.setCreadoPor(domain.getCreatedBy());
        entity.setActualizadoEn(domain.getUpdatedAt());
        entity.setActualizadoPor(domain.getUpdatedBy());

        if (domain.getLineas() != null) {
            for (com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.LineaFactura lineaDomain : domain.getLineas()) {
                LineaFacturaJpaEntity lineaEntity = new LineaFacturaJpaEntity();
                lineaEntity.setId(lineaDomain.getId());
                lineaEntity.setEmpresaId(domain.getEmpresaId());
                lineaEntity.setConcepto(lineaDomain.getConcepto());
                lineaEntity.setCantidad(lineaDomain.getCantidad());
                lineaEntity.setPrecioUnitario(lineaDomain.getPrecioUnitario().monto());
                lineaEntity.setSubtotal(lineaDomain.calcularSubtotal().monto());
                lineaEntity.setTotalImpuestos(lineaDomain.calcularTotalImpuestos().monto());
                lineaEntity.setTotal(lineaDomain.calcularSubtotal().monto().add(lineaDomain.calcularTotalImpuestos().monto()));
                entity.addLinea(lineaEntity);
            }
        }
        return entity;
    }

    public static com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.Factura toLegacyDomain(FacturaJpaEntity entity) {
        if (entity == null) return null;
        List<com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.LineaFactura> lineas = entity.getLineas() != null
                ? entity.getLineas().stream()
                        .map(l -> new com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.LineaFactura(
                                l.getId(),
                                l.getConcepto(),
                                l.getCantidad(),
                                new Dinero(l.getPrecioUnitario(), Dinero.MONEDA_POR_DEFECTO)
                        ))
                        .collect(Collectors.toList())
                : new ArrayList<>();

        return com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.Factura.reconstituir(
                new com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.vo.FacturaId(entity.getId()),
                entity.getEmpresaId(),
                new com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.vo.ClienteId(entity.getClienteId()),
                entity.getPedidoId() != null ? new com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.vo.PedidoId(entity.getPedidoId()) : null,
                new com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.vo.Ruc(entity.getRucCliente() != null ? entity.getRucCliente() : "0000000000000"),
                lineas,
                new ArrayList<>(),
                new Dinero(entity.getSubtotal(), Dinero.MONEDA_POR_DEFECTO),
                new Dinero(entity.getTotalImpuestos(), Dinero.MONEDA_POR_DEFECTO),
                new Dinero(entity.getTotalGeneral(), Dinero.MONEDA_POR_DEFECTO),
                com.SITFAI_CORE_ERP_TIENDA.core.document.domain.model.enums.DocumentStatus.valueOf(entity.getEstado()),
                entity.getVersion() != null ? entity.getVersion() : 0L,
                entity.getCreadoEn(),
                entity.getCreadoPor(),
                entity.getActualizadoEn(),
                entity.getActualizadoPor()
        );
    }
}
