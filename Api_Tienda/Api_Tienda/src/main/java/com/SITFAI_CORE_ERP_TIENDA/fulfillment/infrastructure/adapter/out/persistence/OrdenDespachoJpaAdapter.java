package com.SITFAI_CORE_ERP_TIENDA.fulfillment.infrastructure.adapter.out.persistence;

import com.SITFAI_CORE_ERP_TIENDA.fulfillment.application.port.output.OrdenDespachoRepository;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.model.OrdenDespacho;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.valueobject.DespachoId;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.infrastructure.adapter.out.persistence.entity.OrdenDespachoJpaEntity;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.infrastructure.adapter.out.persistence.repository.OrdenDespachoJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class OrdenDespachoJpaAdapter implements OrdenDespachoRepository {

    private final OrdenDespachoJpaRepository repository;

    public OrdenDespachoJpaAdapter(OrdenDespachoJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public OrdenDespacho guardar(OrdenDespacho orden) {
        OrdenDespachoJpaEntity entity = OrdenDespachoPersistenceMapper.toJpaEntity(orden);
        OrdenDespachoJpaEntity guardada = repository.save(entity);
        return OrdenDespachoPersistenceMapper.toDomainEntity(guardada);
    }

    @Override
    public Optional<OrdenDespacho> buscarPorId(DespachoId id, EmpresaId empresaId) {
        return repository.findByIdAndEmpresaId(id.value().toString(), empresaId.value().toString())
                .map(OrdenDespachoPersistenceMapper::toDomainEntity);
    }
}
