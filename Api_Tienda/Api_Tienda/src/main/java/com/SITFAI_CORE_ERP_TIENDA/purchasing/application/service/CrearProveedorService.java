package com.SITFAI_CORE_ERP_TIENDA.purchasing.application.service;

import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.dto.CrearProveedorRequest;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.dto.ProveedorResponse;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.port.input.CrearProveedorUseCase;
import com.SITFAI_CORE_ERP_TIENDA.sourcing.infrastructure.adapter.out.persistence.entity.ProveedorJpaEntity;
import com.SITFAI_CORE_ERP_TIENDA.sourcing.infrastructure.adapter.out.persistence.repository.ProveedorJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service("purchasingCrearProveedorService")
@Transactional
public class CrearProveedorService implements CrearProveedorUseCase {

    private final ProveedorJpaRepository proveedorJpaRepository;

    public CrearProveedorService(ProveedorJpaRepository proveedorJpaRepository) {
        this.proveedorJpaRepository = proveedorJpaRepository;
    }

    @Override
    public void crearProveedor(UUID empresaId, CrearProveedorRequest request) {
        if (proveedorJpaRepository.existsByEmpresaIdAndRuc(empresaId.toString(), request.ruc())) {
            throw new IllegalArgumentException("Ya existe un proveedor con ese RUC en la base de datos.");
        }

        String id = UUID.randomUUID().toString();
        Instant now = Instant.now();

        ProveedorJpaEntity entity = new ProveedorJpaEntity(
                id,
                empresaId.toString(),
                request.ruc(),
                request.razonSocial(),
                request.emailContacto(),
                request.telefono(),
                request.direccion(),
                request.plazoEntregaDias(),
                "ACTIVO",
                now,
                now,
                true,
                null,
                null
        );

        proveedorJpaRepository.save(entity);
    }

    @Override
    public List<ProveedorResponse> listarProveedores(UUID empresaId) {
        return proveedorJpaRepository.findByEmpresaId(empresaId.toString())
                .stream()
                .map(entity -> new ProveedorResponse(
                        UUID.fromString(entity.getId()),
                        entity.getRuc(),
                        entity.getRazonSocial()
                ))
                .collect(Collectors.toList());
    }
}
