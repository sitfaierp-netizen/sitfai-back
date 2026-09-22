package com.SITFAI_CORE_ERP_TIENDA.purchasing.infrastructure.adapter.out.persistence.adapter;

import com.SITFAI_CORE_ERP_TIENDA.core.document.domain.model.enums.DocumentStatus;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.ordencompra.OrdenCompra;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.ordencompra.port.output.OrdenCompraRepository;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.ordencompra.vo.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.ordencompra.vo.EstadoOrdenCompra;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.ordencompra.vo.OrdenCompraId;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.vo.Dinero;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.infrastructure.adapter.out.persistence.entity.LineaOrdenCompraJpaEntity;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.infrastructure.adapter.out.persistence.entity.OrdenCompraJpaEntity;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.infrastructure.adapter.out.persistence.mapper.OrdenCompraPersistenceMapper;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.infrastructure.adapter.out.persistence.repository.OrdenCompraJpaRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adaptador de Salida JPA (Driven Adapter):
 * Implementa el puerto {@link OrdenCompraRepository} conectando el dominio puro con Spring Data JPA.
 * Mantiene compatibilidad con el puerto legado {@link com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.port.output.OrdenCompraRepository}.
 * <p>
 * Regla MT-01: Exige el {@code empresa_id} en todas las operaciones para blindar el aislamiento multitenant.
 */
@Repository("purchasingOrdenCompraJpaAdapter")
@Primary
public class OrdenCompraJpaAdapter implements OrdenCompraRepository, com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.port.output.OrdenCompraRepository {

    private final OrdenCompraJpaRepository repository;

    public OrdenCompraJpaAdapter(OrdenCompraJpaRepository repository) {
        this.repository = Objects.requireNonNull(repository, "OrdenCompraJpaRepository es obligatorio");
    }

    // ═════════════════════════════════════════════════════════════════════════
    // PUERTO DE DOMINIO ACTUAL: purchasing.domain.model.ordencompra.port.output.OrdenCompraRepository
    // ═════════════════════════════════════════════════════════════════════════

    @Override
    public OrdenCompra guardar(OrdenCompra ordenCompra) {
        Objects.requireNonNull(ordenCompra, "OrdenCompra no puede ser nula");
        OrdenCompraJpaEntity entity = OrdenCompraPersistenceMapper.toEntity(ordenCompra);

        Optional<OrdenCompraJpaEntity> existing = repository.findById(entity.getId());
        OrdenCompraJpaEntity savedEntity;
        if (existing.isPresent()) {
            OrdenCompraJpaEntity e = existing.get();
            e.setEstado(entity.getEstado());
            e.setCostoTotal(entity.getCostoTotal());
            e.setEmitidoEn(entity.getEmitidoEn());
            e.getLineas().clear();
            if (entity.getLineas() != null) {
                entity.getLineas().forEach(l -> {
                    l.setOrdenCompra(e);
                    e.getLineas().add(l);
                });
            }
            savedEntity = repository.save(e);
        } else {
            savedEntity = repository.save(entity);
        }

        return OrdenCompraPersistenceMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<OrdenCompra> buscarPorId(OrdenCompraId id, EmpresaId empresaId) {
        Objects.requireNonNull(id, "OrdenCompraId es obligatorio");
        Objects.requireNonNull(empresaId, "EmpresaId es obligatorio (MT-01)");
        return repository.findByIdAndEmpresaId(id.valor().toString(), empresaId.valor().toString())
                .map(OrdenCompraPersistenceMapper::toDomain);
    }

    @Override
    public List<OrdenCompra> buscarPorEmpresa(EmpresaId empresaId) {
        Objects.requireNonNull(empresaId, "EmpresaId es obligatorio (MT-01)");
        return repository.findByEmpresaId(empresaId.valor().toString()).stream()
                .map(OrdenCompraPersistenceMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrdenCompra> buscarPorEmpresaYEstado(EmpresaId empresaId, EstadoOrdenCompra estado) {
        Objects.requireNonNull(empresaId, "EmpresaId es obligatorio (MT-01)");
        Objects.requireNonNull(estado, "EstadoOrdenCompra es obligatorio");
        return repository.findByEmpresaIdAndEstado(empresaId.valor().toString(), estado.name()).stream()
                .map(OrdenCompraPersistenceMapper::toDomain)
                .collect(Collectors.toList());
    }

    // ═════════════════════════════════════════════════════════════════════════
    // COMPATIBILIDAD CON PUERTO LEGADO
    // ═════════════════════════════════════════════════════════════════════════

    @Override
    public com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.OrdenCompra guardar(
            com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.OrdenCompra ordenCompra) {
        if (ordenCompra == null) return null;

        OrdenCompraJpaEntity entity = toLegacyEntity(ordenCompra);

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

        return toLegacyDomain(savedEntity);
    }

    @Override
    public Optional<com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.OrdenCompra> buscarPorIdYEmpresaId(
            com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.vo.OrdenCompraId id, UUID empresaId) {
        return repository.findByIdAndEmpresaId(id.valor().toString(), empresaId.toString())
                .map(this::toLegacyDomain);
    }

    private OrdenCompraJpaEntity toLegacyEntity(com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.OrdenCompra dominio) {
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

    private com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.OrdenCompra toLegacyDomain(OrdenCompraJpaEntity entity) {
        com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.OrdenCompra orden = com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.OrdenCompra.crear(
                new com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.vo.OrdenCompraId(UUID.fromString(entity.getId())),
                UUID.fromString(entity.getEmpresaId()),
                new com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.vo.ProveedorId(UUID.fromString(entity.getProveedorId())),
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
                    com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.LineaOrdenCompra linea = new com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.LineaOrdenCompra(
                            UUID.randomUUID(),
                            new com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.vo.ProductoId(UUID.fromString(l.getProductoId())),
                            l.getCantidadSolicitada().intValue(),
                            new Dinero(l.getCostoUnitarioEsperado())
                    );
                    @SuppressWarnings("unchecked")
                    List<com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.LineaOrdenCompra> lineas =
                            (List<com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.LineaOrdenCompra>) getField(orden, "lineas");
                    lineas.add(linea);
                } catch (Exception e) {
                    throw new RuntimeException("Error mapeando línea desde JPA", e);
                }
            });

            java.lang.reflect.Method recalc = com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.OrdenCompra.class.getDeclaredMethod("recalcularTotal");
            recalc.setAccessible(true);
            recalc.invoke(orden);

            orden.pullDomainEvents();

        } catch (Exception e) {
            throw new RuntimeException("Error reconstruyendo OrdenCompra desde JPA", e);
        }

        return orden;
    }

    private void setField(Object obj, String name, Object value) throws Exception {
        java.lang.reflect.Field f = com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.OrdenCompra.class.getDeclaredField(name);
        f.setAccessible(true);
        f.set(obj, value);
    }

    private Object getField(Object obj, String name) throws Exception {
        java.lang.reflect.Field f = com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.OrdenCompra.class.getDeclaredField(name);
        f.setAccessible(true);
        return f.get(obj);
    }
}
