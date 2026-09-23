package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.out.persistence.mapper;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.despacho.Despacho;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.despacho.LineaDespacho;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.despacho.vo.DespachoId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.despacho.vo.PedidoId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.BodegaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.Cantidad;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.ProductoId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.out.persistence.entity.DespachoJpaEntity;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.out.persistence.entity.LineaDespachoJpaEntity;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component
public class DespachoPersistenceMapper {

    public DespachoJpaEntity toEntity(Despacho domain) {
        if (domain == null) {
            return null;
        }

        DespachoJpaEntity entity = new DespachoJpaEntity();
        entity.setId(domain.getId().valor());
        entity.setEmpresaId(domain.getEmpresaId().valor());
        entity.setPedidoId(domain.getPedidoId().valor());
        entity.setBodegaId(domain.getBodegaId() != null ? domain.getBodegaId().valor() : null);
        entity.setEstado(domain.getEstado());

        // Audit fields
        entity.setCreadoEn(domain.getCreatedAt() != null ? domain.getCreatedAt() : Instant.now());
        entity.setCreadoPor("SYSTEM");
        entity.setActualizadoEn(domain.getUpdatedAt() != null ? domain.getUpdatedAt() : Instant.now());
        entity.setActualizadoPor("SYSTEM");

        for (LineaDespacho lineaDomain : domain.getLineas()) {
            LineaDespachoJpaEntity lineaEntity = new LineaDespachoJpaEntity();
            lineaEntity.setId(lineaDomain.getId());
            lineaEntity.setEmpresaId(domain.getEmpresaId().valor());
            lineaEntity.setProductoId(lineaDomain.getProductoId().valor());
            lineaEntity.setCantidad(lineaDomain.getCantidad().valor());
            lineaEntity.setCreadoEn(entity.getCreadoEn());
            lineaEntity.setCreadoPor(entity.getCreadoPor());

            entity.agregarLinea(lineaEntity);
        }

        return entity;
    }

    public Despacho toDomain(DespachoJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        List<LineaDespacho> lineas = new ArrayList<>();
        if (entity.getLineas() != null) {
            for (LineaDespachoJpaEntity l : entity.getLineas()) {
                lineas.add(new LineaDespacho(
                        l.getId(),
                        ProductoId.de(l.getProductoId()),
                        Cantidad.de(l.getCantidad())
                ));
            }
        }

        return Despacho.reconstituir(
                DespachoId.de(entity.getId()),
                EmpresaId.de(entity.getEmpresaId()),
                PedidoId.de(entity.getPedidoId()),
                entity.getBodegaId() != null ? BodegaId.de(entity.getBodegaId()) : null,
                entity.getEstado(),
                lineas,
                entity.getCreadoEn() != null ? entity.getCreadoEn() : Instant.now(),
                entity.getActualizadoEn() != null ? entity.getActualizadoEn() : Instant.now()
        );
    }
}
