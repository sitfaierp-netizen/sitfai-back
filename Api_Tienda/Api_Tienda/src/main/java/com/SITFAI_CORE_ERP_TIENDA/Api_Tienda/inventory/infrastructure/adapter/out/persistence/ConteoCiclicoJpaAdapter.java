package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.out.persistence;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.conteo.ConteoCiclico;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.conteo.DetalleConteo;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.conteo.port.ConteoCiclicoRepository;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.conteo.vo.BodegaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.conteo.vo.CantidadFisica;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.conteo.vo.ConteoId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.conteo.vo.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.conteo.vo.EstadoConteo;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.conteo.vo.ProductoId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.out.persistence.entity.ConteoCiclicoJpaEntity;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.out.persistence.entity.DetalleConteoJpaEntity;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.out.persistence.repository.SpringDataConteoCiclicoRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Driven Adapter: Implementación JPA de {@link ConteoCiclicoRepository}.
 * <p>
 * Gestiona la cascada completa y orfandad de la colección {@link DetalleConteoJpaEntity}.
 * Regla MT-01: Aislamiento estricto por {@code empresa_id} en cada operación de persistencia.
 */
@Component("conteoCiclicoJpaAdapter")
public class ConteoCiclicoJpaAdapter implements ConteoCiclicoRepository {

    private final SpringDataConteoCiclicoRepository repository;

    public ConteoCiclicoJpaAdapter(SpringDataConteoCiclicoRepository repository) {
        this.repository = Objects.requireNonNull(repository, "SpringDataConteoCiclicoRepository es obligatorio.");
    }

    @Override
    public void guardar(ConteoCiclico conteo) {
        Objects.requireNonNull(conteo, "ConteoCiclico no puede ser nulo.");

        String conteoIdStr = conteo.getId().valor().toString();
        String empresaIdStr = conteo.getEmpresaId().valor().toString();

        Optional<ConteoCiclicoJpaEntity> existenteOpt = repository.findByIdAndEmpresaId(conteoIdStr, empresaIdStr);

        ConteoCiclicoJpaEntity entity;
        if (existenteOpt.isPresent()) {
            entity = existenteOpt.get();
            entity.setEstado(conteo.getEstado());
            entity.setFechaProgramada(conteo.getFechaProgramada());

            // Actualizar detalles existentes o agregar nuevos
            Map<String, DetalleConteoJpaEntity> detalleEntitiesPorProducto = entity.getDetalles().stream()
                    .collect(Collectors.toMap(DetalleConteoJpaEntity::getProductoId, d -> d));

            for (DetalleConteo dDomain : conteo.getDetalles()) {
                String prodIdStr = dDomain.getProductoId().valor().toString();
                Integer cantFisica = dDomain.getCantidadFisica() != null ? dDomain.getCantidadFisica().valor() : null;

                if (detalleEntitiesPorProducto.containsKey(prodIdStr)) {
                    DetalleConteoJpaEntity dEntity = detalleEntitiesPorProducto.get(prodIdStr);
                    dEntity.setCantidadFisica(cantFisica);
                    dEntity.setCantidadTeorica(dDomain.getCantidadTeorica());
                } else {
                    DetalleConteoJpaEntity nuevoDetalle = new DetalleConteoJpaEntity(
                            dDomain.getId().toString(),
                            empresaIdStr,
                            entity,
                            prodIdStr,
                            dDomain.getCantidadTeorica(),
                            cantFisica
                    );
                    entity.addDetalle(nuevoDetalle);
                }
            }
        } else {
            entity = new ConteoCiclicoJpaEntity(
                    conteoIdStr,
                    empresaIdStr,
                    conteo.getBodegaId().valor().toString(),
                    conteo.getFechaProgramada(),
                    conteo.getEstado()
            );

            for (DetalleConteo dDomain : conteo.getDetalles()) {
                Integer cantFisica = dDomain.getCantidadFisica() != null ? dDomain.getCantidadFisica().valor() : null;
                DetalleConteoJpaEntity detalleEntity = new DetalleConteoJpaEntity(
                        dDomain.getId().toString(),
                        empresaIdStr,
                        entity,
                        dDomain.getProductoId().valor().toString(),
                        dDomain.getCantidadTeorica(),
                        cantFisica
                );
                entity.addDetalle(detalleEntity);
            }
        }

        repository.save(entity);
    }

    @Override
    public Optional<ConteoCiclico> buscarPorId(EmpresaId empresaId, ConteoId id) {
        Objects.requireNonNull(empresaId, "EmpresaId es obligatorio.");
        Objects.requireNonNull(id, "ConteoId es obligatorio.");

        return repository.findByIdAndEmpresaId(id.valor().toString(), empresaId.valor().toString())
                .map(this::toDomain);
    }

    @Override
    public List<ConteoCiclico> listarPorBodega(EmpresaId empresaId, BodegaId bodegaId) {
        Objects.requireNonNull(empresaId, "EmpresaId es obligatorio.");
        Objects.requireNonNull(bodegaId, "BodegaId es obligatorio.");

        return repository.findByEmpresaIdAndBodegaId(empresaId.valor().toString(), bodegaId.valor().toString())
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<ConteoCiclico> listarPorEstado(EmpresaId empresaId, EstadoConteo estado) {
        Objects.requireNonNull(empresaId, "EmpresaId es obligatorio.");
        Objects.requireNonNull(estado, "EstadoConteo es obligatorio.");

        return repository.findByEmpresaIdAndEstado(empresaId.valor().toString(), estado)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public boolean existePorId(EmpresaId empresaId, ConteoId id) {
        Objects.requireNonNull(empresaId, "EmpresaId es obligatorio.");
        Objects.requireNonNull(id, "ConteoId es obligatorio.");

        return repository.existsByIdAndEmpresaId(id.valor().toString(), empresaId.valor().toString());
    }

    private ConteoCiclico toDomain(ConteoCiclicoJpaEntity entity) {
        List<DetalleConteo> detalles = new ArrayList<>();
        if (entity.getDetalles() != null) {
            for (DetalleConteoJpaEntity d : entity.getDetalles()) {
                CantidadFisica cantFisica = d.getCantidadFisica() != null
                        ? CantidadFisica.de(d.getCantidadFisica())
                        : null;
                detalles.add(new DetalleConteo(
                        UUID.fromString(d.getId()),
                        ProductoId.de(d.getProductoId()),
                        d.getCantidadTeorica(),
                        cantFisica
                ));
            }
        }

        return ConteoCiclico.reconstituir(
                ConteoId.de(entity.getId()),
                EmpresaId.de(entity.getEmpresaId()),
                BodegaId.de(entity.getBodegaId()),
                entity.getFechaProgramada(),
                entity.getEstado(),
                detalles,
                entity.getCreadoEn(),
                entity.getActualizadoEn()
        );
    }
}
