package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.application.service;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.application.dto.AgregarComponenteCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.application.dto.AprobarRecetaCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.application.dto.CrearBorradorRecetaCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.application.port.input.GestionarListaMaterialesUseCase;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.application.port.output.TenantProviderPort;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.domain.model.bom.ListaMateriales;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.domain.model.bom.vo.CantidadInsumo;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.domain.model.bom.vo.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.domain.model.bom.vo.InsumoId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.domain.model.bom.vo.ProductoFinalId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.domain.model.bom.vo.RecetaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.domain.port.output.ListaMaterialesRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

@Service
@Transactional
public class GestionarListaMaterialesService implements GestionarListaMaterialesUseCase {

    private final ListaMaterialesRepository repository;
    private final TenantProviderPort tenantProvider;
    private final ApplicationEventPublisher eventPublisher;

    public GestionarListaMaterialesService(ListaMaterialesRepository repository, TenantProviderPort tenantProvider, ApplicationEventPublisher eventPublisher) {
        this.repository = Objects.requireNonNull(repository);
        this.tenantProvider = Objects.requireNonNull(tenantProvider);
        this.eventPublisher = Objects.requireNonNull(eventPublisher);
    }

    @Override
    public UUID crearBorradorReceta(CrearBorradorRecetaCommand command) {
        EmpresaId empresaId = tenantProvider.getEmpresaIdAutenticada();
        RecetaId recetaId = new RecetaId(UUID.randomUUID());
        ProductoFinalId productoFinalId = new ProductoFinalId(command.productoFinalId());

        ListaMateriales bom = new ListaMateriales(empresaId, recetaId, productoFinalId);
        repository.guardar(bom, empresaId);

        return recetaId.valor();
    }

    @Override
    public void agregarComponente(AgregarComponenteCommand command) {
        EmpresaId empresaId = tenantProvider.getEmpresaIdAutenticada();
        RecetaId recetaId = new RecetaId(command.recetaId());
        InsumoId insumoId = new InsumoId(command.insumoId());
        CantidadInsumo cantidad = new CantidadInsumo(command.cantidad());

        ListaMateriales bom = repository.buscarPorId(recetaId, empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Lista de materiales no encontrada"));

        bom.agregarComponente(insumoId, cantidad);
        repository.guardar(bom, empresaId);
    }

    @Override
    public void aprobarReceta(AprobarRecetaCommand command) {
        EmpresaId empresaId = tenantProvider.getEmpresaIdAutenticada();
        RecetaId recetaId = new RecetaId(command.recetaId());

        ListaMateriales bom = repository.buscarPorId(recetaId, empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Lista de materiales no encontrada"));

        bom.aprobar();
        repository.guardar(bom, empresaId);

        // Emitir los eventos de dominio generados (como RecetaProduccionAprobadaEvent)
        bom.obtenerEventosDominio().forEach(eventPublisher::publishEvent);
    }
}
