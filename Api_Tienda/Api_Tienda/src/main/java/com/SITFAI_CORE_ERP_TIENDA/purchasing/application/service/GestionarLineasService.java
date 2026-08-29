package com.SITFAI_CORE_ERP_TIENDA.purchasing.application.service;

import com.SITFAI_CORE_ERP_TIENDA.core.audit.domain.port.ActorProviderPort;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.dto.AgregarLineaCommand;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.dto.OrdenCompraResponse;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.mapper.OrdenCompraApplicationMapper;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.port.input.GestionarLineasUseCase;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.port.output.OrdenCompraRepository;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.exception.DomainException;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.LineaOrdenCompra;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.OrdenCompra;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.vo.Dinero;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.vo.OrdenCompraId;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.vo.ProductoId;
import com.SITFAI_CORE_ERP_TIENDA.shared.infrastructure.security.TenantAuthenticationDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class GestionarLineasService implements GestionarLineasUseCase {

    private final OrdenCompraRepository repository;
    private final ActorProviderPort actorProviderPort;

    public GestionarLineasService(OrdenCompraRepository repository, ActorProviderPort actorProviderPort) {
        this.repository = repository;
        this.actorProviderPort = actorProviderPort;
    }

    @Override
    @Transactional
    public OrdenCompraResponse agregarLinea(AgregarLineaCommand command) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UUID empresaId = null;
        if (authentication != null && authentication.getDetails() instanceof TenantAuthenticationDetails details) {
            empresaId = details.empresaUuid();
        } else {
            empresaId = command.empresaId();
        }

        if (empresaId == null) {
            throw new IllegalArgumentException("empresa_id no encontrado.");
        }

        OrdenCompra orden = repository.buscarPorIdYEmpresaId(
                new OrdenCompraId(command.ordenCompraId()),
                empresaId
        ).orElseThrow(() -> new DomainException("Orden de Compra no encontrada o no pertenece al tenant."));

        orden.setUpdatedBy(actorProviderPort.getCurrentActorId());

        LineaOrdenCompra linea = new LineaOrdenCompra(
                UUID.randomUUID(),
                new ProductoId(command.productoId()),
                command.cantidad().intValue(),
                new Dinero(command.costoUnitario())
        );

        orden.agregarLinea(linea);
        repository.guardar(orden);
        
        return OrdenCompraApplicationMapper.aResponse(orden);
    }
}
