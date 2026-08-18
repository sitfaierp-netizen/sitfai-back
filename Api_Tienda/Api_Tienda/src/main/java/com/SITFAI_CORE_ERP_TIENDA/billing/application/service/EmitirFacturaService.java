package com.SITFAI_CORE_ERP_TIENDA.billing.application.service;

import com.SITFAI_CORE_ERP_TIENDA.billing.application.dto.EmitirFacturaCommand;
import com.SITFAI_CORE_ERP_TIENDA.billing.application.dto.FacturaResponse;
import com.SITFAI_CORE_ERP_TIENDA.billing.application.mapper.FacturaApplicationMapper;
import com.SITFAI_CORE_ERP_TIENDA.billing.application.port.input.EmitirFacturaUseCase;
import com.SITFAI_CORE_ERP_TIENDA.billing.application.port.output.FacturaEventPublisher;
import com.SITFAI_CORE_ERP_TIENDA.billing.application.port.output.FacturaRepository;
import com.SITFAI_CORE_ERP_TIENDA.billing.application.port.output.ResolucionRepository;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.FacturaElectronica;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.LineaFactura;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.valueobject.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Application Service: Orquestador del caso de uso de emitir factura.
 * Cero lógica tributaria, solo coordina la transacción.
 */
@Service
public class EmitirFacturaService implements EmitirFacturaUseCase {

    private final FacturaRepository facturaRepository;
    private final ResolucionRepository resolucionRepository;
    private final FacturaEventPublisher eventPublisher;

    public EmitirFacturaService(FacturaRepository facturaRepository,
                                ResolucionRepository resolucionRepository,
                                FacturaEventPublisher eventPublisher) {
        this.facturaRepository = facturaRepository;
        this.resolucionRepository = resolucionRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public FacturaResponse emitirFactura(EmitirFacturaCommand command) {
        // 1. Validar EmpresaId (MT-01)
        if (command.empresaId() == null) {
            throw new IllegalArgumentException("EmpresaId es obligatorio por política Zero Trust (MT-01)");
        }
        EmpresaId empresaId = new EmpresaId(command.empresaId());
        FacturaId facturaId = new FacturaId(UUID.randomUUID());

        // 2. Obtener Resolución Activa (Delegado a puerto de salida)
        ResolucionDian resolucionDian = resolucionRepository.obtenerActiva(empresaId)
                .orElseThrow(() -> new IllegalStateException("No hay resolución DIAN activa para la empresa"));

        // 3. Instanciar agregado mediante factory method
        FacturaElectronica factura = FacturaElectronica.generarBorrador(
                facturaId,
                empresaId,
                new Nit(command.nitEmisor()),
                new Nit(command.nitReceptor()),
                resolucionDian
        );

        // 4. Añadir líneas de detalle iterando sobre el comando
        for (EmitirFacturaCommand.LineaFacturaDto lineaDto : command.lineas()) {
            LineaFactura linea = new LineaFactura(
                    lineaDto.concepto(),
                    lineaDto.cantidad(),
                    Dinero.de(lineaDto.precioUnitario(), lineaDto.moneda())
            );
            
            // Añadir impuestos a la línea
            for (EmitirFacturaCommand.ImpuestoDto impuestoDto : lineaDto.impuestos()) {
                linea.agregarImpuesto(impuestoDto.tipo(), impuestoDto.tarifa());
            }
            
            factura.agregarLinea(linea);
        }

        // 5. Invocar cálculos en el agregado (La matemática tributaria se ejecuta aquí)
        // Nota: agregarLinea() ya llama a calcularTotales(), pero lo llamamos explícitamente para cumplir la instrucción
        factura.calcularTotales();

        // 6. Persistir el agregado
        facturaRepository.guardar(factura);

        // 7. Publicar eventos de dominio extraídos (Si la hubiéramos firmado aquí, habría eventos)
        factura.getDomainEvents().forEach(eventPublisher::publicar);

        // 8. Retornar DTO de respuesta
        return FacturaApplicationMapper.aResponse(factura);
    }
}
