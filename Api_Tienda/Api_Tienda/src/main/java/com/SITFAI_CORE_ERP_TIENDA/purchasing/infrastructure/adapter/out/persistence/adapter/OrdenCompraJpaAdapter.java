package com.SITFAI_CORE_ERP_TIENDA.purchasing.infrastructure.adapter.out.persistence.adapter;

import com.SITFAI_CORE_ERP_TIENDA.core.document.domain.model.enums.DocumentStatus;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.port.output.OrdenCompraRepository;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.LineaOrdenCompra;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.OrdenCompra;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.vo.Dinero;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.vo.OrdenCompraId;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.vo.ProductoId;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.vo.ProveedorId;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.infrastructure.adapter.out.persistence.entity.LineaOrdenCompraJpaEntity;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.infrastructure.adapter.out.persistence.entity.OrdenCompraJpaEntity;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.infrastructure.adapter.out.persistence.repository.OrdenCompraJpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public class OrdenCompraJpaAdapter implements OrdenCompraRepository {

    private final OrdenCompraJpaRepository repository;

    public OrdenCompraJpaAdapter(OrdenCompraJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public OrdenCompra guardar(OrdenCompra ordenCompra) {
        OrdenCompraJpaEntity entity = toEntity(ordenCompra);

        Optional<OrdenCompraJpaEntity> existing = repository.findById(entity.getId());
        OrdenCompraJpaEntity savedEntity;
        if (existing.isPresent()) {
            OrdenCompraJpaEntity e = existing.get();
            e.setEstado(entity.getEstado());
            e.setCostoTotal(entity.getCostoTotal());
            e.setVersion(entity.getVersion());
            e.setActualizadoEn(ordenCompra.getUpdatedAt());
            e.setActualizadoPor(ordenCompra.getUpdatedBy());
            e.getLineas().clear();
            entity.getLineas().forEach(l -> {
                l.setOrdenCompra(e);
                e.getLineas().add(l);
            });
            savedEntity = repository.save(e);
        } else {
            entity.setCreadoEn(ordenCompra.getCreatedAt());
            entity.setCreadoPor(ordenCompra.getCreatedBy());
            entity.setActualizadoEn(ordenCompra.getUpdatedAt());
            entity.setActualizadoPor(ordenCompra.getUpdatedBy());
            savedEntity = repository.save(entity);
        }
        
        return toDomain(savedEntity);
    }

    @Override
    public Optional<OrdenCompra> buscarPorIdYEmpresaId(OrdenCompraId id, UUID empresaId) {
        return repository.findByIdAndEmpresaId(id.valor().toString(), empresaId.toString())
                .map(this::toDomain);
    }

    private OrdenCompraJpaEntity toEntity(OrdenCompra dominio) {
        OrdenCompraJpaEntity entity = new OrdenCompraJpaEntity(
                dominio.getId().valor().toString(),
                dominio.getEmpresaId().toString(),
                dominio.getProveedorId().valor().toString(),
                dominio.getEstado().name(),
                dominio.getTotalMonetario().monto(),
                dominio.getVersion()
        );

        dominio.getLineas().forEach(linea -> {
            LineaOrdenCompraJpaEntity lineaEntity = new LineaOrdenCompraJpaEntity(
                    dominio.getEmpresaId().toString(),
                    linea.getProductoId().valor().toString(),
                    BigDecimal.valueOf(linea.getCantidad()),
                    linea.getPrecioUnitario().monto(),
                    linea.getSubtotal().monto()
            );
            entity.addLinea(lineaEntity);
        });

        return entity;
    }

    private OrdenCompra toDomain(OrdenCompraJpaEntity entity) {
        OrdenCompra orden = OrdenCompra.crear(
                new OrdenCompraId(UUID.fromString(entity.getId())),
                UUID.fromString(entity.getEmpresaId()),
                new ProveedorId(UUID.fromString(entity.getProveedorId())),
                entity.getCreadoPor() != null ? entity.getCreadoPor() : "system"
        );

        try {
            setField(orden, "estado", DocumentStatus.valueOf(entity.getEstado()));
            setField(orden, "version", entity.getVersion() != null ? entity.getVersion() : 0L);
            setField(orden, "createdAt", entity.getCreadoEn() != null ? entity.getCreadoEn() : Instant.now());
            setField(orden, "updatedAt", entity.getActualizadoEn() != null ? entity.getActualizadoEn() : Instant.now());
            setField(orden, "updatedBy", entity.getActualizadoPor() != null ? entity.getActualizadoPor() : "system");

            entity.getLineas().forEach(l -> {
                try {
                    LineaOrdenCompra linea = new LineaOrdenCompra(
                            UUID.randomUUID(), // Or extract from DB if it had a UUID
                            new ProductoId(UUID.fromString(l.getProductoId())),
                            l.getCantidadSolicitada().intValue(),
                            new Dinero(l.getCostoUnitarioEsperado())
                    );
                    @SuppressWarnings("unchecked")
                    java.util.List<LineaOrdenCompra> lineas = (java.util.List<LineaOrdenCompra>) getField(orden, "lineas");
                    lineas.add(linea);
                } catch (Exception e) {
                    throw new RuntimeException("Error mapeando línea desde JPA", e);
                }
            });

            java.lang.reflect.Method recalc = OrdenCompra.class.getDeclaredMethod("recalcularTotal");
            recalc.setAccessible(true);
            recalc.invoke(orden);
            
            // Limpiar eventos generados por la hidratación (ya que creamos con .crear())
            orden.pullDomainEvents();

        } catch (Exception e) {
            throw new RuntimeException("Error reconstruyendo OrdenCompra desde JPA", e);
        }

        return orden;
    }

    private void setField(Object obj, String name, Object value) throws Exception {
        java.lang.reflect.Field f = OrdenCompra.class.getDeclaredField(name);
        f.setAccessible(true);
        f.set(obj, value);
    }

    private Object getField(Object obj, String name) throws Exception {
        java.lang.reflect.Field f = OrdenCompra.class.getDeclaredField(name);
        f.setAccessible(true);
        return f.get(obj);
    }
}
