package com.SITFAI_CORE_ERP_TIENDA.pos.domain.model;

import com.SITFAI_CORE_ERP_TIENDA.pos.domain.event.DevolucionRegistradaEvent;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.vo.LoteRevertido;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.vo.ProductoId;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.vo.TicketId;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.valueobject.CajaId;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.valueobject.Dinero;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.valueobject.SucursalId;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.valueobject.TurnoId;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.valueobject.UsuarioId;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TurnoCajaTest {

    @Test
    void registrarDevolucion_AfectaBalanceYEmiteEventoCorrectamente() {
        // Arrange
        EmpresaId empresaId = new EmpresaId(UUID.randomUUID());
        CajaId cajaId = new CajaId(UUID.randomUUID());
        SucursalId sucursalId = new SucursalId(UUID.randomUUID());
        UsuarioId usuarioId = new UsuarioId(UUID.randomUUID());
        Dinero montoApertura = new Dinero(new BigDecimal("100.00"));

        TurnoCaja turno = TurnoCaja.abrirTurno(empresaId, cajaId, sucursalId, usuarioId, montoApertura);

        // Venta inicial para tener balance
        turno.registrarTransaccion(TipoTransaccion.VENTA, new Dinero(new BigDecimal("50.00")), "TICKET-1");

        // Act (Devolución)
        TicketId ticketOriginalId = TicketId.generar();
        Dinero montoDevolucion = new Dinero(new BigDecimal("20.00"));
        LoteRevertido lote = new LoteRevertido(new ProductoId(UUID.randomUUID()), "LOTE-XYZ", new BigDecimal("2"));
        
        DevolucionRegistradaEvent.LineaDevolucion linea = new DevolucionRegistradaEvent.LineaDevolucion(UUID.randomUUID(), 2, new BigDecimal("10.00"));
        
        turno.registrarDevolucion(ticketOriginalId, montoDevolucion, List.of(linea), List.of(lote));

        // Assert
        // El balance final debe ser: 100 (apertura) + 50 (venta) - 20 (devolución) = 130
        assertEquals(new BigDecimal("130.00"), turno.calcularConsolidado().valor());
        
        // Debe haberse emitido el evento de DevolucionRegistradaEvent
        var eventos = turno.getDomainEvents().stream()
                .filter(e -> e instanceof DevolucionRegistradaEvent)
                .map(e -> (DevolucionRegistradaEvent) e)
                .toList();

        assertEquals(1, eventos.size());
        DevolucionRegistradaEvent evento = eventos.get(0);
        assertEquals(empresaId, evento.empresaId());
        assertEquals(ticketOriginalId.value(), evento.ventaOrigenId());
        assertEquals(montoDevolucion, evento.montoDevuelto());
        assertEquals(1, evento.lotesRevertidos().size());
        assertEquals("LOTE-XYZ", evento.lotesRevertidos().get(0).codigoLote());
    }
}
