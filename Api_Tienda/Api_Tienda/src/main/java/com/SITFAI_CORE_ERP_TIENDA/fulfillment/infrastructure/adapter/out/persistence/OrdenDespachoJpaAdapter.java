package com.SITFAI_CORE_ERP_TIENDA.fulfillment.infrastructure.adapter.out.persistence;

import com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.model.despacho.LineaDespacho;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.model.despacho.OrdenDespacho;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.port.output.OrdenDespachoRepository;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.model.despacho.vo.BodegaId;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.model.despacho.vo.DespachoId;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.model.despacho.vo.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.model.despacho.vo.EstadoDespacho;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.model.despacho.vo.PedidoId;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.model.despacho.vo.ProductoId;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.infrastructure.adapter.out.persistence.entity.LineaDespachoJpaEntity;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.infrastructure.adapter.out.persistence.entity.OrdenDespachoJpaEntity;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.infrastructure.adapter.out.persistence.repository.SpringDataOrdenDespachoRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class OrdenDespachoJpaAdapter implements OrdenDespachoRepository {

    private final SpringDataOrdenDespachoRepository repository;

    public OrdenDespachoJpaAdapter(SpringDataOrdenDespachoRepository repository) {
        this.repository = repository;
    }

    @Override
    public void guardar(EmpresaId empresaId, OrdenDespacho ordenDespacho) {
        OrdenDespachoJpaEntity entity = repository.findByIdAndEmpresaId(ordenDespacho.getDespachoId().valor().toString(), empresaId.valor().toString())
                .orElseGet(() -> {
                    OrdenDespachoJpaEntity newEntity = new OrdenDespachoJpaEntity();
                    newEntity.setId(ordenDespacho.getDespachoId().valor().toString());
                    newEntity.setEmpresaId(empresaId.valor().toString());
                    newEntity.setPedidoId(ordenDespacho.getPedidoId().valor().toString());
                    newEntity.setBodegaId(ordenDespacho.getBodegaId().valor().toString());
                    return newEntity;
                });
                
        entity.setEstado(ordenDespacho.getEstado());
        
        // Update lines
        entity.getLineas().clear();
        for (LineaDespacho linea : ordenDespacho.getLineas()) {
            LineaDespachoJpaEntity lineaEntity = new LineaDespachoJpaEntity();
            lineaEntity.setId(linea.getId().toString());
            lineaEntity.setProductoId(linea.getProductoId().valor().toString());
            lineaEntity.setCantidad(linea.getCantidad());
            lineaEntity.setOrdenDespacho(entity);
            entity.getLineas().add(lineaEntity);
        }
        
        repository.save(entity);
    }

    @Override
    public Optional<OrdenDespacho> buscarPorId(EmpresaId empresaId, DespachoId despachoId) {
        return repository.findByIdAndEmpresaId(despachoId.valor().toString(), empresaId.valor().toString())
                .map(entity -> {
                    List<LineaDespacho> lineas = entity.getLineas().stream()
                            .map(l -> new LineaDespacho(ProductoId.de(UUID.fromString(l.getProductoId())), l.getCantidad()))
                            .collect(Collectors.toList());
                    OrdenDespacho orden = OrdenDespacho.crear(
                            EmpresaId.de(UUID.fromString(entity.getEmpresaId())),
                            DespachoId.de(UUID.fromString(entity.getId())),
                            PedidoId.de(UUID.fromString(entity.getPedidoId())),
                            BodegaId.de(UUID.fromString(entity.getBodegaId())),
                            lineas
                    );
                    
                    // Recover state (hacky for DDD but necessary for persistence mappings if no state setter)
                    // The standard way is to have an reconstitute method or set state via reflection. 
                    // Let's reflect the state or call transitions up to current state.
                    if (entity.getEstado() == EstadoDespacho.EN_PICKING) {
                        orden.iniciarPicking();
                    } else if (entity.getEstado() == EstadoDespacho.EMPACADO) {
                        orden.iniciarPicking();
                        orden.completarEmpaque();
                    } else if (entity.getEstado() == EstadoDespacho.DESPACHADO) {
                        orden.iniciarPicking();
                        orden.completarEmpaque();
                        orden.confirmarDespacho();
                        orden.pullDomainEvents(); // Clear events caused by rehydration
                    }
                    
                    return orden;
                });
    }
}
