package com.SITFAI_CORE_ERP_TIENDA.fulfillment.infrastructure.adapter.out.persistence;

import com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.model.EstadoDespacho;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.model.LineaDespacho;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.model.OrdenDespacho;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.valueobject.Cantidad;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.valueobject.DespachoId;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.valueobject.DireccionEntrega;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.valueobject.PedidoOrigenId;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.valueobject.ProductoId;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.infrastructure.adapter.out.persistence.entity.LineaDespachoJpaEntity;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.infrastructure.adapter.out.persistence.entity.OrdenDespachoJpaEntity;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.UUID;
import java.util.stream.Collectors;

public class OrdenDespachoPersistenceMapper {

    private OrdenDespachoPersistenceMapper() {}

    public static OrdenDespachoJpaEntity toJpaEntity(OrdenDespacho orden) {
        OrdenDespachoJpaEntity entity = new OrdenDespachoJpaEntity(
                orden.getId().value().toString(),
                orden.getEmpresaId().value().toString(),
                orden.getPedidoOrigenId().value().toString(),
                orden.getEstado().name(),
                orden.getDireccionEntrega().direccionLocal(),
                orden.getDireccionEntrega().ciudad(),
                orden.getDireccionEntrega().codigoPostal()
        );

        entity.setLineas(orden.getLineas().stream()
                .map(linea -> new LineaDespachoJpaEntity(
                        linea.getId().toString(),
                        orden.getEmpresaId().value().toString(),
                        linea.getProductoId().value().toString(),
                        BigDecimal.valueOf(linea.getCantidadSolicitada().value()),
                        BigDecimal.valueOf(linea.getCantidadPreparada().value())
                ))
                .collect(Collectors.toList()));

        return entity;
    }

    public static OrdenDespacho toDomainEntity(OrdenDespachoJpaEntity entity) {
        try {
            // Reconstrucción del agregado
            OrdenDespacho orden = OrdenDespacho.planificar(
                    new EmpresaId(UUID.fromString(entity.getEmpresaId())),
                    new PedidoOrigenId(UUID.fromString(entity.getPedidoOrigenId())),
                    new DireccionEntrega(entity.getDireccionLocal(), entity.getCiudad(), entity.getCodigoPostal()),
                    entity.getLineas().stream().map(l -> LineaDespacho.crear(new ProductoId(UUID.fromString(l.getProductoId())), Cantidad.de(l.getCantidadSolicitada().intValue()))).collect(Collectors.toList())
            );

            // Set ID y Estado usando Reflexión
            setField(orden, "id", new DespachoId(UUID.fromString(entity.getId())));
            setField(orden, "estado", EstadoDespacho.valueOf(entity.getEstado()));

            // Set atributos de líneas
            @SuppressWarnings("unchecked")
            java.util.List<LineaDespacho> lineasDominio = (java.util.List<LineaDespacho>) getField(orden, "lineas");
            
            for (int i = 0; i < entity.getLineas().size(); i++) {
                LineaDespachoJpaEntity lJpa = entity.getLineas().get(i);
                LineaDespacho lDom = lineasDominio.get(i);
                
                setField(lDom, "id", UUID.fromString(lJpa.getId()));
                setField(lDom, "cantidadPreparada", Cantidad.de(lJpa.getCantidadPreparada().intValue()));
            }

            return orden;
        } catch (Exception e) {
            throw new RuntimeException("Error mapeando OrdenDespachoJpaEntity a Dominio", e);
        }
    }

    private static void setField(Object obj, String fieldName, Object value) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }
    
    private static Object getField(Object obj, String fieldName) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(obj);
    }
}
