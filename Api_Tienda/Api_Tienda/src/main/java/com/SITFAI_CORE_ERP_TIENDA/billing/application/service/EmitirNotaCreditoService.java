package com.SITFAI_CORE_ERP_TIENDA.billing.application.service;

import com.SITFAI_CORE_ERP_TIENDA.billing.application.dto.EmitirNotaCreditoCommand;
import com.SITFAI_CORE_ERP_TIENDA.billing.application.dto.NotaCreditoResponse;
import com.SITFAI_CORE_ERP_TIENDA.billing.application.mapper.NotaCreditoApplicationMapper;
import com.SITFAI_CORE_ERP_TIENDA.billing.application.port.input.EmitirNotaCreditoUseCase;
import com.SITFAI_CORE_ERP_TIENDA.billing.application.port.output.FacturaEventPublisher;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.port.output.FacturaRepository;
import com.SITFAI_CORE_ERP_TIENDA.billing.application.port.output.NotaCreditoRepository;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.exception.BillingRuleException;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.FacturaElectronica;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.LineaFactura;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.NotaCreditoElectronica;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.valueobject.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Application Service: Orquesta la emisión de una Nota de Crédito.
 * Valida la existencia de la factura original y delega cálculos al dominio.
 */
@Service
public class EmitirNotaCreditoService implements EmitirNotaCreditoUseCase {

    private final NotaCreditoRepository notaCreditoRepository;
    private final FacturaRepository facturaRepository;
    private final FacturaEventPublisher eventPublisher;

    public EmitirNotaCreditoService(NotaCreditoRepository notaCreditoRepository,
                                    FacturaRepository facturaRepository,
                                    FacturaEventPublisher eventPublisher) {
        this.notaCreditoRepository = notaCreditoRepository;
        this.facturaRepository = facturaRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public NotaCreditoResponse emitirNotaCredito(EmitirNotaCreditoCommand command) {
        // 1. Validación de EmpresaId (Regla MT-01)
        if (command.empresaId() == null) {
            throw new IllegalArgumentException("El EmpresaId es obligatorio para la orquestación aislada.");
        }
        
        EmpresaId empresaId = new EmpresaId(command.empresaId());
        FacturaId facturaAfectadaId = new FacturaId(command.facturaAfectadaId());
        
        // 2. Búsqueda de la Factura Original (Invariante DIAN)
        com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.Factura facturaOriginal = facturaRepository.findByIdAndEmpresaId(new com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.vo.FacturaId(command.facturaAfectadaId()), command.empresaId())
                .orElseThrow(() -> new BillingRuleException("La Factura original a afectar no existe o no pertenece a la empresa."));
                
        Cufe cufeFactura = new Cufe("CUFE-PENDIENTE-INTEGRACION");
        
        // 3. Instanciación del Agregado de Nota de Crédito (Generar Borrador)
        NotaCreditoId notaCreditoId = new NotaCreditoId(UUID.randomUUID());
        MotivoDevolucion motivo = new MotivoDevolucion(command.codigoMotivo(), command.descripcionMotivo());
        
        NotaCreditoElectronica notaCredito = NotaCreditoElectronica.generarBorrador(
                notaCreditoId,
                empresaId,
                facturaAfectadaId,
                cufeFactura,
                motivo
        );

        // 4. Inyección de las líneas a reversar
        for (EmitirNotaCreditoCommand.LineaReversoDto lineaDto : command.lineas()) {
            LineaFactura linea = new LineaFactura(
                    lineaDto.concepto(),
                    lineaDto.cantidad(),
                    Dinero.de(lineaDto.precioUnitario(), lineaDto.moneda())
            );
            
            for (EmitirNotaCreditoCommand.ImpuestoReversoDto impuestoDto : lineaDto.impuestos()) {
                linea.agregarImpuesto(impuestoDto.tipo(), impuestoDto.tarifa());
            }
            
            notaCredito.agregarLineaReverso(linea);
        }

        // 5. Cálculos (Lógica matemática pura de Java ejecutada dentro del dominio)
        // Nota: agregarLineaReverso() ya actualiza los totales por dentro, pero aseguramos la invocación.
        notaCredito.calcularTotalesReverso();

        // 6. Persistencia
        notaCreditoRepository.guardar(notaCredito);

        // 7. Publicar Eventos de Dominio (si los hay en este estado)
        notaCredito.getDomainEvents().forEach(eventPublisher::publicar);

        // 8. Mapeo a Response
        return NotaCreditoApplicationMapper.aResponse(notaCredito);
    }
}
