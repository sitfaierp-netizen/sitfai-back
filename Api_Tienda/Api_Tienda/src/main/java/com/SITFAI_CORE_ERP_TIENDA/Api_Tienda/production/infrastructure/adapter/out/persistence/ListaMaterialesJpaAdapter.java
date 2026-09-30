package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.infrastructure.adapter.out.persistence;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.domain.model.bom.ComponenteReceta;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.domain.model.bom.ListaMateriales;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.domain.model.bom.vo.*;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.domain.port.output.ListaMaterialesRepository;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.infrastructure.adapter.out.persistence.entity.ComponenteRecetaJpaEntity;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.infrastructure.adapter.out.persistence.entity.ListaMaterialesJpaEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@org.springframework.transaction.annotation.Transactional
public class ListaMaterialesJpaAdapter implements ListaMaterialesRepository {

    private final ListaMaterialesSpringDataRepository repository;

    public ListaMaterialesJpaAdapter(ListaMaterialesSpringDataRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    @Override
    public void guardar(ListaMateriales listaMateriales, EmpresaId empresaId) {
        if (!listaMateriales.getEmpresaId().valor().equals(empresaId.valor())) {
            throw new IllegalArgumentException("EmpresaId mismatch (MT-01)");
        }

        ListaMaterialesJpaEntity entity = repository.findByIdAndEmpresaId(listaMateriales.getId().valor(), empresaId.valor())
                .orElse(new ListaMaterialesJpaEntity());

        entity.setId(listaMateriales.getId().valor());
        entity.setEmpresaId(listaMateriales.getEmpresaId().valor());
        entity.setProductoFinalId(listaMateriales.getProductoFinalId().valor());
        entity.setEstado(listaMateriales.getEstado().name());

        // Manejo de la coleccin
        entity.getComponentes().clear();
        for (ComponenteReceta compDomain : listaMateriales.getComponentes()) {
            ComponenteRecetaJpaEntity compEntity = new ComponenteRecetaJpaEntity();
            compEntity.setId(compDomain.getId());
            compEntity.setInsumoId(compDomain.getInsumoId().valor());
            compEntity.setCantidad(compDomain.getCantidad().valor());
            compEntity.setListaMateriales(entity);
            entity.getComponentes().add(compEntity);
        }

        repository.save(entity);
    }

    @Override
    public Optional<ListaMateriales> buscarPorId(RecetaId id, EmpresaId empresaId) {
        return repository.findByIdAndEmpresaId(id.valor(), empresaId.valor())
                .map(entity -> {
                    List<ComponenteReceta> componentes = entity.getComponentes().stream()
                            .map(compEntity -> new ComponenteReceta(
                                    compEntity.getId(),
                                    new InsumoId(compEntity.getInsumoId()),
                                    new CantidadInsumo(compEntity.getCantidad())
                            ))
                            .collect(Collectors.toList());

                    return ListaMateriales.reconstituir(
                            new EmpresaId(entity.getEmpresaId()),
                            new RecetaId(entity.getId()),
                            new ProductoFinalId(entity.getProductoFinalId()),
                            EstadoReceta.valueOf(entity.getEstado()),
                            componentes
                    );
                });
    }
}
