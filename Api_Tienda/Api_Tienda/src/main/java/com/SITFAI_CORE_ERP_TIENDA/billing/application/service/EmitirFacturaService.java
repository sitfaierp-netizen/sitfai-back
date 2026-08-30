package com.SITFAI_CORE_ERP_TIENDA.billing.application.service;

import com.SITFAI_CORE_ERP_TIENDA.billing.application.dto.EmitirFacturaCommand;
import com.SITFAI_CORE_ERP_TIENDA.billing.application.dto.FacturaResponse;
import com.SITFAI_CORE_ERP_TIENDA.billing.application.mapper.FacturaApplicationMapper;
import com.SITFAI_CORE_ERP_TIENDA.billing.application.port.input.EmitirFacturaUseCase;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.Factura;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.vo.ClienteId;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.vo.FacturaId;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.vo.PedidoId;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.vo.Ruc;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.port.output.FacturaRepository;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.valueobject.Dinero;
import com.SITFAI_CORE_ERP_TIENDA.core.audit.domain.port.ActorProviderPort;
import com.SITFAI_CORE_ERP_TIENDA.billing.application.port.output.FacturaEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class EmitirFacturaService implements EmitirFacturaUseCase {

    private final FacturaRepository facturaRepository;
    private final ActorProviderPort actorProvider;
    private final FacturaEventPublisher eventPublisher;

    public EmitirFacturaService(FacturaRepository facturaRepository,
                                ActorProviderPort actorProvider,
                                FacturaEventPublisher eventPublisher) {
        this.facturaRepository = facturaRepository;
        this.actorProvider = actorProvider;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public FacturaResponse emitirFactura(EmitirFacturaCommand command) {
        // 1. Extraer empresaId y usuario de ActorProvider (Zero Trust MT-02)
        UUID empresaId = actorProvider.getCurrentEmpresaId();
        String usuario = actorProvider.getCurrentUsername();

        FacturaId facturaId = new FacturaId(UUID.randomUUID());
        ClienteId clienteId = new ClienteId(command.clienteId());
        PedidoId pedidoId = command.pedidoId() != null ? new PedidoId(command.pedidoId()) : null;
        Ruc rucCliente = new Ruc(command.rucCliente());

        // 2. Instanciar agregado Factura
        Factura factura = Factura.crear(facturaId, empresaId, clienteId, pedidoId, rucCliente, usuario);

        // 3. Añadir líneas e impuestos
        for (EmitirFacturaCommand.LineaFacturaCommand lineaDto : command.lineas()) {
            Dinero precioUnitario = Dinero.de(lineaDto.precioUnitario(), lineaDto.moneda());
            factura.agregarLinea(lineaDto.concepto(), lineaDto.cantidad(), precioUnitario);
            
            if (lineaDto.impuestos() != null) {
                for (EmitirFacturaCommand.ImpuestoCommand impuestoDto : lineaDto.impuestos()) {
                    factura.aplicarImpuesto(impuestoDto.tipo(), impuestoDto.tarifa());
                }
            }
        }

        // 4. Emitir factura (cambia estado a EMITIDO y genera FacturaEmitidaEvent)
        factura.emitir();

        // 5. Persistir agregado
        facturaRepository.save(factura);

        // 6. Publicar eventos de dominio
        factura.pullDomainEvents().forEach(eventPublisher::publicar);

        // 7. Retornar DTO
        return FacturaApplicationMapper.toResponse(factura);
    }
}
