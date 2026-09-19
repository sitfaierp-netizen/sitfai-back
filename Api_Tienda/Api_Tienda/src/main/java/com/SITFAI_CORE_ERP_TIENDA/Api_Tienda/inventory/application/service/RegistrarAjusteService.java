package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.service;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.RegistrarAjusteCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.port.input.RegistrarAjusteUseCase;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.port.output.TenantProviderPort;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.event.AjusteAplicadoEvent;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.Bodega;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.ajuste.AjusteInventario;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.ajuste.LineaAjuste;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.ajuste.vo.AjusteInventarioId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.ajuste.vo.MotivoAjuste;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.recepcion.vo.Lote;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.port.output.AjusteInventarioRepository;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.port.output.BodegaEventPublisher;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.port.output.BodegaRepository;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.BodegaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.DocumentoFuenteId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.LoteId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.ProductoId;
import com.SITFAI_CORE_ERP_TIENDA.core.audit.domain.port.ActorProviderPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistrarAjusteService implements RegistrarAjusteUseCase {

    private final AjusteInventarioRepository ajusteRepository;
    private final BodegaRepository bodegaRepository;
    private final BodegaEventPublisher eventPublisher;
    private final TenantProviderPort tenantProviderPort;
    private final ActorProviderPort actorProviderPort;

    public RegistrarAjusteService(
            AjusteInventarioRepository ajusteRepository,
            BodegaRepository bodegaRepository,
            BodegaEventPublisher eventPublisher,
            TenantProviderPort tenantProviderPort,
            ActorProviderPort actorProviderPort) {
        this.ajusteRepository = ajusteRepository;
        this.bodegaRepository = bodegaRepository;
        this.eventPublisher = eventPublisher;
        this.tenantProviderPort = tenantProviderPort;
        this.actorProviderPort = actorProviderPort;
    }

    @Override
    @Transactional
    public AjusteInventarioId ejecutar(RegistrarAjusteCommand command) {
        // 1. Extraer contexto seguro (MT-01 y AUD-01)
        EmpresaId empresaId = tenantProviderPort.getEmpresaIdAutenticada();
        String actor = actorProviderPort.getCurrentActorId();
        BodegaId bodegaId = BodegaId.de(command.bodegaId());

        // 2. Cargar Bodega asegurando que pertenece a la empresa
        Bodega bodega = bodegaRepository.buscarPorId(bodegaId, empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Bodega no encontrada o no pertenece a la empresa actual."));

        // 3. Crear AjusteInventario
        AjusteInventarioId ajusteId = AjusteInventarioId.generar();
        MotivoAjuste motivo = MotivoAjuste.valueOf(command.motivo().toUpperCase());
        AjusteInventario ajuste = AjusteInventario.crearBorrador(ajusteId, empresaId, bodegaId, motivo, actor);

        DocumentoFuenteId documentoFuenteId = new DocumentoFuenteId("AJUSTE", ajusteId.valor().toString());

        // 4. Procesar líneas y aplicarlas a la Bodega
        command.lineas().forEach(lineaDto -> {
            ProductoId productoId = ProductoId.de(lineaDto.productoId());
            LoteId loteId = lineaDto.codigoLote() != null ? LoteId.de(lineaDto.codigoLote()) : null;
            Lote lote = lineaDto.codigoLote() != null ? new Lote(lineaDto.codigoLote(), lineaDto.fechaCaducidad()) : null;

            LineaAjuste lineaAjuste = new LineaAjuste(productoId, lote, lineaDto.diferencia());
            ajuste.agregarLinea(lineaAjuste);

            java.time.Instant fechaCaducidadInstant = lineaDto.fechaCaducidad() != null 
                ? lineaDto.fechaCaducidad().atStartOfDay(java.time.ZoneId.systemDefault()).toInstant() 
                : null;

            // Delega la mutación matemática al Agregado Bodega (BOD-03)
            bodega.aplicarAjuste(productoId, lineaDto.diferencia(), loteId, fechaCaducidadInstant, documentoFuenteId);
        });

        // 5. Confirmar documento
        ajuste.confirmarAjuste();

        // 6. Persistir
        bodegaRepository.guardar(bodega);
        ajusteRepository.guardar(ajuste);

        // 7. Publicar eventos de dominio
        eventPublisher.publicarTodos(bodega.drainDomainEvents());
        eventPublisher.publicar(AjusteAplicadoEvent.of(empresaId, bodegaId, ajusteId));

        return ajusteId;
    }
}
