package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.out.persistence.mapper;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.recepcion.LineaRecepcion;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.recepcion.Recepcion;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.recepcion.vo.Lote;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.recepcion.vo.OrdenCompraId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.recepcion.vo.RecepcionId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.BodegaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.Cantidad;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.ProductoId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.out.persistence.entity.LineaRecepcionJpaEntity;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.out.persistence.entity.RecepcionJpaEntity;
import com.SITFAI_CORE_ERP_TIENDA.core.document.domain.model.enums.DocumentStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component
public class RecepcionPersistenceMapper {

    public RecepcionJpaEntity toEntity(Recepcion domain) {
        if (domain == null) {
            return null;
        }
        
        RecepcionJpaEntity entity = new RecepcionJpaEntity();
        entity.setId(domain.getId().valor());
        entity.setEmpresaId(domain.getEmpresaId().valor());
        entity.setBodegaDestinoId(domain.getBodegaDestino().valor());
        entity.setOrdenCompraOrigenId(domain.getOrdenCompraOrigen().valor());
        entity.setEstado(domain.getEstado());
        
        // Audit fields
        entity.setCreadoEn(domain.getCreatedAt());
        entity.setCreadoPor(domain.getCreatedBy());
        
        for (LineaRecepcion lineaDomain : domain.getLineas()) {
            LineaRecepcionJpaEntity lineaEntity = new LineaRecepcionJpaEntity();
            lineaEntity.setId(lineaDomain.getId());
            lineaEntity.setProductoId(lineaDomain.getProductoId().valor());
            lineaEntity.setCantidadRecibida(lineaDomain.getCantidadRecibida().valor());
            lineaEntity.setCodigoLote(lineaDomain.getLote().codigoLote());
            lineaEntity.setFechaCaducidad(lineaDomain.getLote().fechaCaducidad());
            
            entity.agregarLinea(lineaEntity);
        }
        
        return entity;
    }

    public Recepcion toDomain(RecepcionJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        
        // We must reconstitute the aggregate without violating its invariants,
        // but since we are mapping from DB, we use reflection to bypass business checks.
        Recepcion recepcion = Recepcion.crearBorrador(
                new RecepcionId(entity.getId()),
                new EmpresaId(entity.getEmpresaId()),
                new BodegaId(entity.getBodegaDestinoId()),
                new OrdenCompraId(entity.getOrdenCompraOrigenId()),
                entity.getCreadoPor()
        );
        
        setField(recepcion, "estado", entity.getEstado());
        setField(recepcion, "createdAt", entity.getCreadoEn());
        
        List<LineaRecepcion> lineas = new ArrayList<>();
        for (LineaRecepcionJpaEntity lineaEntity : entity.getLineas()) {
            LineaRecepcion linea = new LineaRecepcion(
                    new ProductoId(lineaEntity.getProductoId()),
                    new Cantidad(lineaEntity.getCantidadRecibida()),
                    new Lote(lineaEntity.getCodigoLote(), lineaEntity.getFechaCaducidad())
            );
            setField(linea, "id", lineaEntity.getId());
            lineas.add(linea);
        }
        setField(recepcion, "lineas", lineas);
        
        return recepcion;
    }

    private void setField(Object object, String fieldName, Object value) {
        Field field = ReflectionUtils.findField(object.getClass(), fieldName);
        if (field != null) {
            ReflectionUtils.makeAccessible(field);
            ReflectionUtils.setField(field, object, value);
        }
    }
}
