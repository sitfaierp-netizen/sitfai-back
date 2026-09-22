package com.SITFAI_CORE_ERP_TIENDA.billing.infrastructure.adapter.out.persistence;

import com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.factura.Factura;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.factura.port.FacturaRepository;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.factura.vo.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.factura.vo.FacturaId;
import com.SITFAI_CORE_ERP_TIENDA.billing.infrastructure.adapter.out.persistence.entity.FacturaJpaEntity;
import com.SITFAI_CORE_ERP_TIENDA.billing.infrastructure.adapter.out.persistence.mapper.FacturaPersistenceMapper;
import com.SITFAI_CORE_ERP_TIENDA.billing.infrastructure.adapter.out.persistence.repository.SpringDataFacturaRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adaptador de Salida JPA (Driven Adapter / SPI):
 * Implementa el puerto {@link FacturaRepository} conectando el dominio con Spring Data JPA.
 * <p>
 * Regla MT-01: Exige el {@code empresa_id} en todas las consultas para garantizar aislamiento físico/lógico.
 */
@Component("billingFacturaJpaAdapter")
@Primary
public class FacturaJpaAdapter implements FacturaRepository, com.SITFAI_CORE_ERP_TIENDA.billing.domain.port.output.FacturaRepository {

    private final SpringDataFacturaRepository repository;

    public FacturaJpaAdapter(SpringDataFacturaRepository repository) {
        this.repository = Objects.requireNonNull(repository, "SpringDataFacturaRepository es obligatorio");
    }

    // ═════════════════════════════════════════════════════════════════════════
    // PUERTO DE DOMINIO ACTUAL: billing.domain.model.factura.port.FacturaRepository
    // ═════════════════════════════════════════════════════════════════════════

    @Override
    public Factura guardar(Factura factura) {
        Objects.requireNonNull(factura, "Factura no puede ser nula");
        FacturaJpaEntity entity = FacturaPersistenceMapper.toEntity(factura);
        FacturaJpaEntity guardada = repository.save(entity);
        return FacturaPersistenceMapper.toDomain(guardada);
    }

    @Override
    public Optional<Factura> buscarPorId(FacturaId id, EmpresaId empresaId) {
        Objects.requireNonNull(id, "FacturaId es obligatorio");
        Objects.requireNonNull(empresaId, "EmpresaId es obligatorio (MT-01)");
        return repository.findByIdAndEmpresaId(id.valor(), empresaId.valor())
                .map(FacturaPersistenceMapper::toDomain);
    }

    @Override
    public List<Factura> buscarPorEmpresa(EmpresaId empresaId) {
        Objects.requireNonNull(empresaId, "EmpresaId es obligatorio (MT-01)");
        return repository.findByEmpresaId(empresaId.valor()).stream()
                .map(FacturaPersistenceMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existePorId(FacturaId id, EmpresaId empresaId) {
        Objects.requireNonNull(id, "FacturaId es obligatorio");
        Objects.requireNonNull(empresaId, "EmpresaId es obligatorio (MT-01)");
        return repository.existsByIdAndEmpresaId(id.valor(), empresaId.valor());
    }

    // ═════════════════════════════════════════════════════════════════════════
    // COMPATIBILIDAD CON PUERTO LEGADO: billing.domain.port.output.FacturaRepository
    // ═════════════════════════════════════════════════════════════════════════

    @Override
    public void save(com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.Factura factura) {
        if (factura == null) return;
        FacturaJpaEntity entity = FacturaPersistenceMapper.toEntity(factura);
        repository.save(entity);
    }

    @Override
    public Optional<com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.Factura> findByIdAndEmpresaId(
            com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.vo.FacturaId id, UUID empresaId) {
        if (id == null || empresaId == null) return Optional.empty();
        return repository.findByIdAndEmpresaId(id.value(), empresaId)
                .map(FacturaPersistenceMapper::toLegacyDomain);
    }
}
