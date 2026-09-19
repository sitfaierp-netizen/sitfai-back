package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.out.persistence.mapper.ajuste;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.ajuste.AjusteInventario;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.ajuste.LineaAjuste;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.ajuste.vo.AjusteInventarioId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.recepcion.vo.Lote;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.BodegaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.ProductoId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.out.persistence.jpa.entity.ajuste.AjusteInventarioJpaEntity;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.out.persistence.jpa.entity.ajuste.LineaAjusteJpaEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class AjusteInventarioJpaMapper {

    public AjusteInventarioJpaEntity toEntity(AjusteInventario domain) {
        if (domain == null) return null;

        AjusteInventarioJpaEntity entity = new AjusteInventarioJpaEntity();
        entity.setId(domain.getId().valor());
        entity.setEmpresaId(domain.getEmpresaId().valor());
        entity.setBodegaId(domain.getBodegaId().valor());
        entity.setMotivo(domain.getMotivo());
        entity.setEstado(domain.getEstado());
        entity.setCreadoEn(domain.getCreatedAt());
        entity.setCreadoPor(domain.getCreatedBy());

        List<LineaAjusteJpaEntity> lineas = domain.getLineas().stream().map(l -> {
            LineaAjusteJpaEntity lineaEntity = new LineaAjusteJpaEntity();
            lineaEntity.setId(l.getId());
            lineaEntity.setProductoId(l.getProductoId().valor());
            lineaEntity.setDiferencia(l.getDiferencia());
            if (l.getLote() != null) {
                lineaEntity.setCodigoLote(l.getLote().codigoLote());
                lineaEntity.setFechaCaducidad(l.getLote().fechaCaducidad());
            }
            return lineaEntity;
        }).collect(Collectors.toList());

        entity.setLineas(lineas);
        return entity;
    }

    public AjusteInventario toDomain(AjusteInventarioJpaEntity entity) {
        if (entity == null) return null;

        List<LineaAjuste> lineas = entity.getLineas().stream().map(l -> {
            Lote lote = l.getCodigoLote() != null ? new Lote(l.getCodigoLote(), l.getFechaCaducidad()) : null;
            return new LineaAjuste(ProductoId.de(l.getProductoId()), lote, l.getDiferencia());
        }).collect(Collectors.toList());

        return AjusteInventario.reconstituir(
                AjusteInventarioId.de(entity.getId()),
                EmpresaId.de(entity.getEmpresaId()),
                BodegaId.de(entity.getBodegaId()),
                entity.getMotivo(),
                entity.getCreadoPor(),
                entity.getCreadoEn(),
                entity.getEstado(),
                lineas
        );
    }
}
