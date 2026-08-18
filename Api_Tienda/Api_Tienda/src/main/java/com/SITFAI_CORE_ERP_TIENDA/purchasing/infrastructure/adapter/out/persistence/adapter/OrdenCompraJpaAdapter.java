package com.SITFAI_CORE_ERP_TIENDA.purchasing.infrastructure.adapter.out.persistence.adapter;

import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.port.output.OrdenCompraRepository;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.EstadoOrden;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.LineaOrdenCompra;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.OrdenCompra;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.valueobject.Dinero;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.valueobject.OrdenCompraId;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.valueobject.ProductoId;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.valueobject.ProveedorId;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.infrastructure.adapter.out.persistence.entity.LineaOrdenCompraJpaEntity;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.infrastructure.adapter.out.persistence.entity.OrdenCompraJpaEntity;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.infrastructure.adapter.out.persistence.repository.OrdenCompraJpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador de Salida JPA para OrdenCompra (Hexagonal).
 * Implementa OrdenCompraRepository usando las firmas corretas (.valor()).
 */
@Repository
public class OrdenCompraJpaAdapter implements OrdenCompraRepository {

    private final OrdenCompraJpaRepository repository;

    public OrdenCompraJpaAdapter(OrdenCompraJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public void guardar(OrdenCompra ordenCompra) {
        OrdenCompraJpaEntity entity = toEntity(ordenCompra);

        Optional<OrdenCompraJpaEntity> existing = repository.findById(entity.getId());
        if (existing.isPresent()) {
            OrdenCompraJpaEntity e = existing.get();
            e.setEstado(entity.getEstado());
            e.setCostoTotal(entity.getCostoTotal());
            e.getLineas().clear();
            entity.getLineas().forEach(l -> {
                l.setOrdenCompra(e);
                e.getLineas().add(l);
            });
            repository.save(e);
        } else {
            repository.save(entity);
        }
    }

    @Override
    public Optional<OrdenCompra> buscarPorId(OrdenCompraId id, EmpresaId empresaId) {
        return repository.findByIdAndEmpresaId(id.valor().toString(), empresaId.valor().toString())
                .map(this::toDomain);
    }

    // ─── Mapper Interno (evita deuda técnica de paquete) ───────────────────────

    private OrdenCompraJpaEntity toEntity(OrdenCompra dominio) {
        OrdenCompraJpaEntity entity = new OrdenCompraJpaEntity(
                dominio.getId().valor().toString(),
                dominio.getEmpresaId().valor().toString(),
                dominio.getProveedorId().valor().toString(),
                dominio.getFechaCreacion(),
                dominio.getEstado().name(),
                dominio.getCostoTotal().monto()
        );

        dominio.getLineas().forEach(linea -> {
            LineaOrdenCompraJpaEntity lineaEntity = new LineaOrdenCompraJpaEntity(
                    dominio.getEmpresaId().valor().toString(),
                    linea.getProductoId().valor().toString(),
                    linea.getCantidadSolicitada(),
                    linea.getCostoUnitarioPactado().monto(),
                    linea.calcularSubtotal().monto()
            );
            entity.addLinea(lineaEntity);
        });

        return entity;
    }

    private OrdenCompra toDomain(OrdenCompraJpaEntity entity) {
        OrdenCompra orden = OrdenCompra.crearBorrador(
                new OrdenCompraId(UUID.fromString(entity.getId())),
                new EmpresaId(UUID.fromString(entity.getEmpresaId())),
                new ProveedorId(UUID.fromString(entity.getProveedorId()))
        );

        try {
            setField(orden, "estado", EstadoOrden.valueOf(entity.getEstado()));
            setField(orden, "fechaCreacion", entity.getFechaCreacion());

            entity.getLineas().forEach(l -> {
                try {
                    LineaOrdenCompra linea = new LineaOrdenCompra(
                            new ProductoId(UUID.fromString(l.getProductoId())),
                            l.getCantidadSolicitada(),
                            new Dinero(l.getCostoUnitarioEsperado(), "COP")
                    );
                    @SuppressWarnings("unchecked")
                    java.util.List<LineaOrdenCompra> lineas = (java.util.List<LineaOrdenCompra>) getField(orden, "lineas");
                    lineas.add(linea);
                } catch (Exception e) {
                    throw new RuntimeException("Error mapeando línea desde JPA", e);
                }
            });

            // Recalcular total
            java.lang.reflect.Method recalc = OrdenCompra.class.getDeclaredMethod("recalcularCostoTotal");
            recalc.setAccessible(true);
            recalc.invoke(orden);

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
