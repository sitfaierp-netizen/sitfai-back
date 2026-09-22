package com.SITFAI_CORE_ERP_TIENDA.billing.application.service;

import com.SITFAI_CORE_ERP_TIENDA.billing.application.dto.EmitirFacturaCommand;
import com.SITFAI_CORE_ERP_TIENDA.billing.application.dto.FacturaResponse;
import com.SITFAI_CORE_ERP_TIENDA.billing.application.port.input.EmitirFacturaUseCase;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.factura.Factura;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.factura.LineaFactura;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.factura.port.FacturaRepository;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.factura.vo.ClienteId;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.factura.vo.DocumentoFuenteId;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.factura.vo.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.factura.vo.FacturaId;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.valueobject.Dinero;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Servicio de Aplicación: Orquestador del caso de uso de Emisión de Facturas.
 * <p>
 * Regla 1: Orquesta el dominio, llama al factory method del agregado Factura,
 * persiste mediante el puerto FacturaRepository garantizando el aislamiento MT-01,
 * y publica los eventos de dominio mediante ApplicationEventPublisher.
 */
@Service
public class EmitirFacturaService implements EmitirFacturaUseCase {

    private static final Logger log = LoggerFactory.getLogger(EmitirFacturaService.class);

    private final FacturaRepository facturaRepository;
    private final ApplicationEventPublisher eventPublisher;

    public EmitirFacturaService(FacturaRepository facturaRepository,
                                ApplicationEventPublisher eventPublisher) {
        this.facturaRepository = Objects.requireNonNull(facturaRepository, "FacturaRepository es obligatorio");
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "ApplicationEventPublisher es obligatorio");
    }

    @Override
    @Transactional
    public FacturaResponse emitirFactura(EmitirFacturaCommand command) {
        Objects.requireNonNull(command, "EmitirFacturaCommand no puede ser nulo");

        // 1. Validar aislamiento Multitenant (MT-01)
        if (command.empresaId() == null) {
            throw new IllegalArgumentException("EmpresaId es obligatorio (MT-01)");
        }

        // 2. Preparar Value Objects de Dominio
        FacturaId facturaId = FacturaId.generar();
        EmpresaId empresaId = EmpresaId.de(command.empresaId());
        ClienteId clienteId = command.clienteId() != null
                ? ClienteId.de(command.clienteId())
                : ClienteId.generar();

        DocumentoFuenteId documentoFuenteId;
        if (command.documentoFuenteId() != null && command.tipoOrigen() != null) {
            documentoFuenteId = DocumentoFuenteId.de(command.tipoOrigen(), command.documentoFuenteId().toString());
        } else if (command.pedidoId() != null) {
            documentoFuenteId = DocumentoFuenteId.ecommerce(command.pedidoId().toString());
        } else if (command.documentoFuenteId() != null) {
            documentoFuenteId = DocumentoFuenteId.de("VENTA", command.documentoFuenteId().toString());
        } else {
            documentoFuenteId = DocumentoFuenteId.de("DIRECTA", UUID.randomUUID().toString());
        }

        // 3. Mapear líneas de comando a entidades de Dominio protegidas
        List<LineaFactura> lineasDominio = new ArrayList<>();
        for (EmitirFacturaCommand.LineaFacturaCommand lineaCmd : command.lineas()) {
            Dinero precioUnitario = Dinero.de(
                    lineaCmd.precioUnitario(),
                    lineaCmd.moneda() != null ? lineaCmd.moneda() : "COP"
            );
            LineaFactura linea = LineaFactura.crear(
                    lineaCmd.concepto(),
                    lineaCmd.cantidad(),
                    precioUnitario
            );
            if (lineaCmd.impuestos() != null) {
                for (EmitirFacturaCommand.ImpuestoCommand imp : lineaCmd.impuestos()) {
                    linea.agregarImpuesto(imp.tipo(), imp.tarifa());
                }
            }
            lineasDominio.add(linea);
        }

        // 4. Instanciar Agregado Factura mediante Factory Method (calcula totales y valida fail-fast)
        Factura factura = Factura.emitir(
                facturaId,
                empresaId,
                documentoFuenteId,
                clienteId,
                lineasDominio
        );

        // 5. Persistir agregado a través del Output Port (MT-01)
        Factura facturaGuardada = facturaRepository.guardar(factura);

        // 6. Publicar eventos de dominio acumulados mediante ApplicationEventPublisher
        factura.drainDomainEvents().forEach(evento -> {
            log.debug("Publicando evento de dominio de facturación: {}", evento);
            eventPublisher.publishEvent(evento);
        });

        log.info("Factura {} emitida y persistida exitosamente para la empresa {}",
                facturaGuardada.getId().valor(), empresaId.valor());

        // 7. Mapear y retornar FacturaResponse
        return new FacturaResponse(
                facturaGuardada.getId().valor().toString(),
                facturaGuardada.getEmpresaId().valor().toString(),
                facturaGuardada.getClienteId().valor().toString(),
                command.pedidoId() != null ? command.pedidoId().toString() : null,
                command.rucCliente(),
                facturaGuardada.getSubtotal().monto(),
                facturaGuardada.getTotalImpuestos().monto(),
                facturaGuardada.getTotal().monto(),
                facturaGuardada.getEstado().name(),
                facturaGuardada.getLineas().stream()
                        .map(l -> new FacturaResponse.LineaFacturaResponse(
                                l.getDescripcion(),
                                l.getCantidad(),
                                l.getPrecioUnitario().monto(),
                                l.calcularSubtotal().monto(),
                                l.calcularTotalImpuestos().monto()
                        ))
                        .collect(Collectors.toList())
        );
    }
}
