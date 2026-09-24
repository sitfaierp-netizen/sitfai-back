package com.SITFAI_CORE_ERP_TIENDA.replenishment.infrastructure.adapter.out.persistence;

import com.SITFAI_CORE_ERP_TIENDA.replenishment.domain.model.politica.PoliticaInventario;
import com.SITFAI_CORE_ERP_TIENDA.replenishment.domain.model.politica.vo.BodegaId;
import com.SITFAI_CORE_ERP_TIENDA.replenishment.domain.model.politica.vo.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.replenishment.domain.model.politica.vo.NivelOptimo;
import com.SITFAI_CORE_ERP_TIENDA.replenishment.domain.model.politica.vo.PoliticaId;
import com.SITFAI_CORE_ERP_TIENDA.replenishment.domain.model.politica.vo.ProductoId;
import com.SITFAI_CORE_ERP_TIENDA.replenishment.domain.model.politica.vo.PuntoReorden;
import com.SITFAI_CORE_ERP_TIENDA.replenishment.domain.port.output.PoliticaInventarioRepository;
import com.SITFAI_CORE_ERP_TIENDA.replenishment.infrastructure.adapter.out.persistence.entity.PoliticaInventarioJpaEntity;
import com.SITFAI_CORE_ERP_TIENDA.replenishment.infrastructure.adapter.out.persistence.repository.SpringDataPoliticaInventarioRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Driven Adapter: Implementa PoliticaInventarioRepository usando JPA.
 * Traduce Dominio ↔ JPA Entity. El Dominio nunca ve esta clase.
 * <p>
 * Regla REGLA-6: Adaptador de salida que implementa el puerto de dominio.
 * Regla MT-01: Toda operación de BD incluye empresaId como discriminador.
 */
@Repository
public class PoliticaInventarioJpaAdapter implements PoliticaInventarioRepository {

    private final SpringDataPoliticaInventarioRepository jpaRepository;

    public PoliticaInventarioJpaAdapter(SpringDataPoliticaInventarioRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public PoliticaInventario guardar(PoliticaInventario politica) {
        PoliticaInventarioJpaEntity entity = toEntity(politica);
        jpaRepository.save(entity);
        return politica;
    }

    @Override
    public Optional<PoliticaInventario> buscarPorId(PoliticaId id, EmpresaId empresaId) {
        return jpaRepository.findByIdAndEmpresaId(
                        id.valor().toString(),
                        empresaId.valor().toString())
                .map(this::toDomain);
    }

    @Override
    public Optional<PoliticaInventario> buscarActivaPorBodegaYProducto(BodegaId bodegaId, ProductoId productoId, EmpresaId empresaId) {
        return jpaRepository.findByBodegaIdAndProductoIdAndEmpresaIdAndActivaTrue(
                        bodegaId.valor().toString(),
                        productoId.valor().toString(),
                        empresaId.valor().toString())
                .map(this::toDomain);
    }

    // -------------------------------------------------------------------------
    // Mappers privados: Dominio → JPA Entity
    // -------------------------------------------------------------------------
    private PoliticaInventarioJpaEntity toEntity(PoliticaInventario p) {
        return new PoliticaInventarioJpaEntity(
                p.getId().valor().toString(),
                p.getEmpresaId().valor().toString(),
                p.getBodegaId().valor().toString(),
                p.getProductoId().valor().toString(),
                p.getPuntoReorden().valor(),
                p.getNivelOptimo().valor(),
                p.isActiva()
        );
    }

    // -------------------------------------------------------------------------
    // Mappers privados: JPA Entity → Dominio
    // -------------------------------------------------------------------------
    private PoliticaInventario toDomain(PoliticaInventarioJpaEntity e) {
        return new PoliticaInventario(
                new PoliticaId(UUID.fromString(e.getId())),
                new EmpresaId(UUID.fromString(e.getEmpresaId())),
                new BodegaId(UUID.fromString(e.getBodegaId())),
                new ProductoId(UUID.fromString(e.getProductoId())),
                new PuntoReorden(e.getPuntoReorden()),
                new NivelOptimo(e.getNivelOptimo()),
                e.isActiva()
        );
    }
}
