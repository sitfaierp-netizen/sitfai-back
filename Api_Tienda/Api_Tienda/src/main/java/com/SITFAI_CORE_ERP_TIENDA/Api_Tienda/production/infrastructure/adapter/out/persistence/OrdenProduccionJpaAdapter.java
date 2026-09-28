package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.infrastructure.adapter.out.persistence;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.domain.model.orden.OrdenProduccion;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.domain.model.orden.vo.*;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.domain.port.output.OrdenProduccionRepository;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.infrastructure.adapter.out.persistence.entity.OrdenProduccionJpaEntity;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

@Component
public class OrdenProduccionJpaAdapter implements OrdenProduccionRepository {

    private final OrdenProduccionSpringDataRepository repository;

    public OrdenProduccionJpaAdapter(OrdenProduccionSpringDataRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    @Override
    public void guardar(OrdenProduccion ordenProduccion, EmpresaId empresaId) {
        if (!ordenProduccion.getEmpresaId().valor().equals(empresaId.valor())) {
            throw new IllegalArgumentException("EmpresaId mismatch (MT-01)");
        }

        OrdenProduccionJpaEntity entity = repository.findByIdAndEmpresaId(ordenProduccion.getId().valor(), empresaId.valor())
                .orElse(new OrdenProduccionJpaEntity());

        entity.setId(ordenProduccion.getId().valor());
        entity.setEmpresaId(ordenProduccion.getEmpresaId().valor());
        entity.setRecetaId(ordenProduccion.getRecetaId().valor());
        entity.setBodegaId(ordenProduccion.getBodegaId().valor());
        entity.setCantidadProducir(ordenProduccion.getCantidadProducir().valor());
        entity.setEstado(ordenProduccion.getEstado().name());

        repository.save(entity);
    }

    @Override
    public Optional<OrdenProduccion> buscarPorId(OrdenProduccionId id, EmpresaId empresaId) {
        return repository.findByIdAndEmpresaId(id.valor(), empresaId.valor())
                .map(entity -> OrdenProduccion.reconstituir(
                        new EmpresaId(entity.getEmpresaId()),
                        new OrdenProduccionId(entity.getId()),
                        new RecetaId(entity.getRecetaId()),
                        new BodegaId(entity.getBodegaId()),
                        new CantidadProducir(entity.getCantidadProducir()),
                        EstadoOrdenProduccion.valueOf(entity.getEstado())
                ));
    }
}
