package com.SITFAI_CORE_ERP_TIENDA.purchasing.application.service;

import com.SITFAI_CORE_ERP_TIENDA.core.audit.domain.port.ActorProviderPort;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.dto.CrearBorradorCommand;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.dto.OrdenCompraResponse;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.mapper.OrdenCompraApplicationMapper;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.port.input.CrearOrdenUseCase;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.port.output.OrdenCompraRepository;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.OrdenCompra;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.vo.OrdenCompraId;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.vo.ProveedorId;
import com.SITFAI_CORE_ERP_TIENDA.shared.infrastructure.security.TenantAuthenticationDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CrearOrdenService implements CrearOrdenUseCase {

    private final OrdenCompraRepository repository;
    private final ActorProviderPort actorProviderPort;

    public CrearOrdenService(OrdenCompraRepository repository, ActorProviderPort actorProviderPort) {
        this.repository = repository;
        this.actorProviderPort = actorProviderPort;
    }

    @Override
    @Transactional
    public OrdenCompraResponse crearBorrador(CrearBorradorCommand command) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UUID empresaId = null;
        if (authentication != null && authentication.getDetails() instanceof TenantAuthenticationDetails details) {
            empresaId = details.empresaUuid();
        } else {
            // Fallback al comando (por compatibilidad en tests sin contexto completo o si es un proceso asincrono interno)
            empresaId = command.empresaId();
        }
        
        if (empresaId == null) {
            throw new IllegalArgumentException("empresa_id no encontrado en el contexto de seguridad.");
        }

        String createdBy = actorProviderPort.getCurrentActorId();

        OrdenCompra orden = OrdenCompra.crear(
                OrdenCompraId.generar(),
                empresaId,
                new ProveedorId(command.proveedorId()),
                createdBy
        );

        repository.guardar(orden);
        return OrdenCompraApplicationMapper.aResponse(orden);
    }
}
