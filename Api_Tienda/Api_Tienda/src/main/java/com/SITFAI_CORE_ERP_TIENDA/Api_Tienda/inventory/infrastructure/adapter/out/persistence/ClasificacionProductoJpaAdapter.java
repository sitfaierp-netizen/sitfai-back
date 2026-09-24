package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.out.persistence;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.clasificacion.ClasificacionProducto;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.clasificacion.port.ClasificacionProductoRepository;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.clasificacion.vo.AnalisisId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.clasificacion.vo.BodegaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.clasificacion.vo.CategoriaABC;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.clasificacion.vo.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.clasificacion.vo.MetricaMovimiento;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.clasificacion.vo.ProductoId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.out.persistence.entity.ClasificacionProductoJpaEntity;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.out.persistence.repository.SpringDataClasificacionProductoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Driven Adapter: Implementa {@link ClasificacionProductoRepository} usando Spring Data JPA.
 * <p>
 * Regla REGLA-6: Adaptador de salida desacoplado del dominio.
 * Regla MT-01: Exige empresaId en todas las firmas para garantizar aislamiento por tenant.
 */
@Repository
public class ClasificacionProductoJpaAdapter implements ClasificacionProductoRepository {

    private final SpringDataClasificacionProductoRepository jpaRepository;

    public ClasificacionProductoJpaAdapter(SpringDataClasificacionProductoRepository jpaRepository) {
        this.jpaRepository = Objects.requireNonNull(jpaRepository, "SpringDataClasificacionProductoRepository no puede ser nulo");
    }

    @Override
    public void guardar(ClasificacionProducto clasificacion) {
        Objects.requireNonNull(clasificacion, "clasificacion no puede ser nula");

        String idStr = clasificacion.getId().valor().toString();
        String empresaIdStr = clasificacion.getEmpresaId().valor().toString();
        String bodegaIdStr = clasificacion.getBodegaId().valor().toString();
        String productoIdStr = clasificacion.getProductoId().valor().toString();

        ClasificacionProductoJpaEntity entity = jpaRepository.findById(idStr)
                .or(() -> jpaRepository.findByEmpresaIdAndBodegaIdAndProductoId(empresaIdStr, bodegaIdStr, productoIdStr))
                .orElseGet(() -> new ClasificacionProductoJpaEntity(
                        idStr,
                        empresaIdStr,
                        bodegaIdStr,
                        productoIdStr,
                        clasificacion.getCategoria(),
                        clasificacion.getMetrica().frecuenciaSalida(),
                        clasificacion.getMetrica().valorTotalDespachado()
                ));

        entity.setCategoria(clasificacion.getCategoria());
        entity.setFrecuenciaSalida(clasificacion.getMetrica().frecuenciaSalida());
        entity.setValorTotalDespachado(clasificacion.getMetrica().valorTotalDespachado());

        jpaRepository.save(entity);
    }

    @Override
    public Optional<ClasificacionProducto> buscarPorProducto(EmpresaId empresaId, BodegaId bodegaId, ProductoId productoId) {
        Objects.requireNonNull(empresaId, "empresaId no puede ser nulo");
        Objects.requireNonNull(bodegaId, "bodegaId no puede ser nulo");
        Objects.requireNonNull(productoId, "productoId no puede ser nulo");

        return jpaRepository.findByEmpresaIdAndBodegaIdAndProductoId(
                empresaId.valor().toString(),
                bodegaId.valor().toString(),
                productoId.valor().toString()
        ).map(this::toDomain);
    }

    @Override
    public Optional<ClasificacionProducto> buscarPorId(EmpresaId empresaId, AnalisisId id) {
        Objects.requireNonNull(empresaId, "empresaId no puede ser nulo");
        Objects.requireNonNull(id, "id no puede ser nulo");

        return jpaRepository.findByIdAndEmpresaId(
                id.valor().toString(),
                empresaId.valor().toString()
        ).map(this::toDomain);
    }

    @Override
    public List<ClasificacionProducto> listarPorBodega(EmpresaId empresaId, BodegaId bodegaId) {
        Objects.requireNonNull(empresaId, "empresaId no puede ser nulo");
        Objects.requireNonNull(bodegaId, "bodegaId no puede ser nulo");

        return jpaRepository.findByEmpresaIdAndBodegaId(
                empresaId.valor().toString(),
                bodegaId.valor().toString()
        ).stream().map(this::toDomain).toList();
    }

    private ClasificacionProducto toDomain(ClasificacionProductoJpaEntity entity) {
        return ClasificacionProducto.reconstituir(
                new AnalisisId(UUID.fromString(entity.getId())),
                new EmpresaId(UUID.fromString(entity.getEmpresaId())),
                new BodegaId(UUID.fromString(entity.getBodegaId())),
                new ProductoId(UUID.fromString(entity.getProductoId())),
                MetricaMovimiento.de(entity.getFrecuenciaSalida(), entity.getValorTotalDespachado()),
                entity.getCategoria(),
                entity.getCreadoEn()
        );
    }
}
