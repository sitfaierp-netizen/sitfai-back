package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.out.persistence;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.recepcion.LineaRecepcion;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.recepcion.RecepcionMercancia;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.recepcion.vo.*;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.port.output.RecepcionMercanciaRepository;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.out.persistence.entity.LineaRecepcionJpaEntity;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.out.persistence.entity.RecepcionMercanciaJpaEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@org.springframework.transaction.annotation.Transactional
public class RecepcionMercanciaJpaAdapter implements RecepcionMercanciaRepository {

    private final RecepcionMercanciaSpringDataRepository repository;

    public RecepcionMercanciaJpaAdapter(RecepcionMercanciaSpringDataRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    @Override
    public void guardar(RecepcionMercancia recepcion, EmpresaId empresaId) {
        if (!recepcion.getEmpresaId().valor().equals(empresaId.valor())) {
            throw new IllegalArgumentException("EmpresaId mismatch (MT-01)");
        }
        
        RecepcionMercanciaJpaEntity entity = repository.findByIdAndEmpresaId(recepcion.getId().valor(), empresaId.valor())
                .orElse(new RecepcionMercanciaJpaEntity());
                
        entity.setId(recepcion.getId().valor());
        entity.setEmpresaId(recepcion.getEmpresaId().valor());
        entity.setOrdenCompraId(recepcion.getOrdenCompraId().valor());
        entity.setBodegaId(recepcion.getBodegaId().valor());
        entity.setEstado(recepcion.getEstado().name());
        
        // Map lines
        entity.getLineas().clear();
        for (LineaRecepcion lineaDomain : recepcion.getLineas()) {
            LineaRecepcionJpaEntity lineaEntity = new LineaRecepcionJpaEntity();
            lineaEntity.setId(lineaDomain.getId());
            lineaEntity.setProductoId(lineaDomain.getProductoId().valor());
            lineaEntity.setCantidadEsperada(lineaDomain.getCantidadEsperada().valor());
            lineaEntity.setCantidadRecibida(lineaDomain.getCantidadRecibida().valor());
            lineaEntity.setRecepcion(entity);
            entity.getLineas().add(lineaEntity);
        }
        
        repository.save(entity);
    }

    @Override
    public Optional<RecepcionMercancia> buscarPorId(RecepcionId id, EmpresaId empresaId) {
        return repository.findByIdAndEmpresaId(id.valor(), empresaId.valor())
                .map(entity -> {
                    List<LineaRecepcion> lineas = entity.getLineas().stream()
                            .map(lineaEntity -> new LineaRecepcion(
                                    lineaEntity.getId(),
                                    new ProductoId(lineaEntity.getProductoId()),
                                    new CantidadRecepcion(lineaEntity.getCantidadEsperada()),
                                    new CantidadRecepcion(lineaEntity.getCantidadRecibida())
                            ))
                            .collect(Collectors.toList());

                    RecepcionMercancia recepcion = RecepcionMercancia.reconstituir(
                            new EmpresaId(entity.getEmpresaId()),
                            new RecepcionId(entity.getId()),
                            new OrdenCompraId(entity.getOrdenCompraId()),
                            new BodegaId(entity.getBodegaId()),
                            EstadoRecepcion.valueOf(entity.getEstado()),
                            lineas
                    );
                    
                    return recepcion;
                });
    }
}
