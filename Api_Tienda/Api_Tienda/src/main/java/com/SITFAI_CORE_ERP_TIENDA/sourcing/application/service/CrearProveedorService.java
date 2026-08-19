package com.SITFAI_CORE_ERP_TIENDA.sourcing.application.service;

import com.SITFAI_CORE_ERP_TIENDA.sourcing.application.dto.CrearProveedorCommand;
import com.SITFAI_CORE_ERP_TIENDA.sourcing.application.dto.ProveedorResponse;
import com.SITFAI_CORE_ERP_TIENDA.sourcing.application.port.input.CrearProveedorUseCase;
import com.SITFAI_CORE_ERP_TIENDA.sourcing.application.port.output.ProveedorRepository;
import com.SITFAI_CORE_ERP_TIENDA.sourcing.application.port.output.SourcingEventPublisherPort;
import com.SITFAI_CORE_ERP_TIENDA.sourcing.application.port.output.TenantProviderPort;
import com.SITFAI_CORE_ERP_TIENDA.sourcing.domain.model.Proveedor;
import com.SITFAI_CORE_ERP_TIENDA.sourcing.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.sourcing.domain.valueobject.ProveedorId;
import com.SITFAI_CORE_ERP_TIENDA.sourcing.domain.valueobject.Ruc;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@Transactional
public class CrearProveedorService implements CrearProveedorUseCase {

    private final TenantProviderPort tenantProvider;
    private final ProveedorRepository proveedorRepository;
    private final SourcingEventPublisherPort eventPublisher;

    public CrearProveedorService(TenantProviderPort tenantProvider,
                                 ProveedorRepository proveedorRepository,
                                 SourcingEventPublisherPort eventPublisher) {
        this.tenantProvider = Objects.requireNonNull(tenantProvider);
        this.proveedorRepository = Objects.requireNonNull(proveedorRepository);
        this.eventPublisher = Objects.requireNonNull(eventPublisher);
    }

    @Override
    public ProveedorResponse crear(CrearProveedorCommand command) {
        // 1. Resolver tenant activo (MT-01)
        EmpresaId empresaId = EmpresaId.de(tenantProvider.obtenerEmpresaIdActual());

        // 2. Crear Aggregate Root
        Proveedor proveedor = Proveedor.crear(
                ProveedorId.generar(),
                empresaId,
                new Ruc(command.ruc()),
                command.razonSocial(),
                command.emailContacto(),
                command.telefono(),
                command.direccion(),
                command.plazoEntregaDias()
        );

        // 3. Persistir
        proveedorRepository.guardar(proveedor);

        // 4. Drenar eventos
        proveedor.drenaEventos().forEach(eventPublisher::publicar);

        // 5. Mapear DTO
        return new ProveedorResponse(
                proveedor.getProveedorId().valor(),
                proveedor.getEmpresaId().valor(),
                proveedor.getRuc().valor(),
                proveedor.getRazonSocial(),
                proveedor.getEmailContacto(),
                proveedor.getTelefono(),
                proveedor.getDireccion(),
                proveedor.getPlazoEntregaDias(),
                proveedor.getEstado().name(),
                proveedor.getCreadoEn()
        );
    }
}
