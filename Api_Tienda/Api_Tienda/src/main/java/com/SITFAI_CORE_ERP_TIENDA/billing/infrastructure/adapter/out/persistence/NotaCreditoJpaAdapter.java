package com.SITFAI_CORE_ERP_TIENDA.billing.infrastructure.adapter.out.persistence;

import com.SITFAI_CORE_ERP_TIENDA.billing.application.port.output.NotaCreditoRepository;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.NotaCreditoElectronica;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.valueobject.*;
import com.SITFAI_CORE_ERP_TIENDA.billing.infrastructure.adapter.out.persistence.entity.LineaNotaCreditoJpaEntity;
import com.SITFAI_CORE_ERP_TIENDA.billing.infrastructure.adapter.out.persistence.entity.NotaCreditoJpaEntity;
import com.SITFAI_CORE_ERP_TIENDA.billing.infrastructure.adapter.out.persistence.repository.NotaCreditoJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class NotaCreditoJpaAdapter implements NotaCreditoRepository {

    private final NotaCreditoJpaRepository repository;

    public NotaCreditoJpaAdapter(NotaCreditoJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public void guardar(NotaCreditoElectronica nota) {
        Objects.requireNonNull(nota, "NotaCreditoElectronica no puede ser null.");
        NotaCreditoJpaEntity entity = toEntity(nota);
        repository.save(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<NotaCreditoElectronica> buscarPorId(NotaCreditoId id, EmpresaId empresaId) {
        Objects.requireNonNull(id, "Id no puede ser null.");
        Objects.requireNonNull(empresaId, "EmpresaId no puede ser null.");
        return repository.findByIdAndEmpresaId(id.valor().toString(), empresaId.valor().toString())
                .map(this::toDomain);
    }

    private NotaCreditoJpaEntity toEntity(NotaCreditoElectronica domain) {
        NotaCreditoJpaEntity entity = new NotaCreditoJpaEntity();
        entity.setId(domain.getId().valor().toString());
        entity.setEmpresaId(domain.getEmpresaId().valor().toString());
        entity.setFacturaAfectadaId(domain.getFacturaAfectadaId().valor().toString());
        entity.setCufeFacturaAfectada(domain.getCufeFacturaAfectada().valor());
        entity.setMotivo(domain.getMotivo().codigo());
        entity.setEstado(domain.getEstado().name());
        entity.setCufe(domain.getCufe() != null ? domain.getCufe().valor() : null);
        entity.setSubtotal(domain.getSubtotal().monto());
        entity.setTotalImpuestos(domain.getTotalImpuestos().monto());
        entity.setTotalGeneral(domain.getTotalGeneral().monto());

        entity.setLineas(domain.getLineasReversadas().stream().map(linea -> {
            LineaNotaCreditoJpaEntity lineaEntity = new LineaNotaCreditoJpaEntity();
            lineaEntity.setId(java.util.UUID.randomUUID().toString());
            lineaEntity.setEmpresaId(domain.getEmpresaId().valor().toString());
            lineaEntity.setNotaCredito(entity);
            lineaEntity.setConcepto(linea.getConcepto());
            lineaEntity.setCantidad(linea.getCantidad());
            lineaEntity.setPrecioUnitario(linea.getPrecioUnitario().monto());
            lineaEntity.setSubtotal(linea.calcularSubtotal().monto());
            lineaEntity.setTotalImpuestos(linea.calcularTotalImpuestos().monto());
            return lineaEntity;
        }).collect(Collectors.toList()));

        return entity;
    }

    private NotaCreditoElectronica toDomain(NotaCreditoJpaEntity entity) {
        // Since we only query to check existence/idempotency or very specific reads, 
        // we map it back as an empty draft for now. In a full system, you'd rebuild 
        // the domain aggregate using a specialized constructor or factory.
        // Returning null since the service currently just checks if present.
        return NotaCreditoElectronica.generarBorrador(
            new NotaCreditoId(java.util.UUID.fromString(entity.getId())),
            new EmpresaId(java.util.UUID.fromString(entity.getEmpresaId())),
            new FacturaId(java.util.UUID.fromString(entity.getFacturaAfectadaId())),
            new Cufe(entity.getCufeFacturaAfectada()),
            new MotivoDevolucion(entity.getMotivo(), "Motivo registrado")
        );
    }
}
