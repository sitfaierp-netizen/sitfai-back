package com.SITFAI_CORE_ERP_TIENDA.returns.domain.model.rma;

import com.SITFAI_CORE_ERP_TIENDA.returns.domain.model.rma.event.ProductoAprobadoParaReingresoEvent;
import com.SITFAI_CORE_ERP_TIENDA.returns.domain.model.rma.event.ProductoRechazadoAMermaEvent;
import com.SITFAI_CORE_ERP_TIENDA.returns.domain.model.rma.vo.CantidadDevuelta;
import com.SITFAI_CORE_ERP_TIENDA.returns.domain.model.rma.vo.DevolucionId;
import com.SITFAI_CORE_ERP_TIENDA.returns.domain.model.rma.vo.DocumentoFuenteId;
import com.SITFAI_CORE_ERP_TIENDA.returns.domain.model.rma.vo.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.returns.domain.model.rma.vo.EstadoRma;
import com.SITFAI_CORE_ERP_TIENDA.returns.domain.model.rma.vo.MotivoDevolucion;
import com.SITFAI_CORE_ERP_TIENDA.returns.domain.model.rma.vo.ProductoId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AutorizacionDevolucionTest {

    private EmpresaId empresaId;
    private DevolucionId devolucionId;
    private DocumentoFuenteId documentoFuenteId;
    private ProductoId producto1;
    private ProductoId producto2;

    @BeforeEach
    void setUp() {
        empresaId = EmpresaId.de(UUID.randomUUID());
        devolucionId = DevolucionId.generar();
        documentoFuenteId = DocumentoFuenteId.de(UUID.randomUUID());
        producto1 = ProductoId.de(UUID.randomUUID());
        producto2 = ProductoId.de(UUID.randomUUID());
    }

    @Test
    void debeInicializarseEnEstadoAutorizada() {
        var linea = new LineaDevolucion(producto1, CantidadDevuelta.de(2), MotivoDevolucion.de("Defectuoso"));
        AutorizacionDevolucion rma = AutorizacionDevolucion.emitir(empresaId, devolucionId, documentoFuenteId, List.of(linea));

        assertThat(rma.getEstado()).isEqualTo(EstadoRma.AUTORIZADA);
        assertThat(rma.getLineas()).hasSize(1);
    }

    @Test
    void debePrevenirInspeccionAntesDeRecibirFisicamente() {
        var linea = new LineaDevolucion(producto1, CantidadDevuelta.de(2), MotivoDevolucion.de("Defectuoso"));
        AutorizacionDevolucion rma = AutorizacionDevolucion.emitir(empresaId, devolucionId, documentoFuenteId, List.of(linea));

        assertThatThrownBy(() -> rma.inspeccionar(producto1, true))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Solo se puede inspeccionar");
    }

    @Test
    void flujoCompletoAprobado() {
        var linea1 = new LineaDevolucion(producto1, CantidadDevuelta.de(2), MotivoDevolucion.de("Caja abierta"));
        var linea2 = new LineaDevolucion(producto2, CantidadDevuelta.de(1), MotivoDevolucion.de("No era lo esperado"));
        
        AutorizacionDevolucion rma = AutorizacionDevolucion.emitir(empresaId, devolucionId, documentoFuenteId, List.of(linea1, linea2));
        rma.recibirFisicamente();
        assertThat(rma.getEstado()).isEqualTo(EstadoRma.RECIBIDA_EN_CUARENTENA);

        // Inspeccionamos la primera
        rma.inspeccionar(producto1, true);
        assertThat(rma.getEstado()).isEqualTo(EstadoRma.INSPECCION_PARCIAL);
        
        // Verificamos el evento
        List<Object> events = rma.pullDomainEvents();
        assertThat(events).hasSize(1);
        assertThat(events.get(0)).isInstanceOf(ProductoAprobadoParaReingresoEvent.class);

        // Inspeccionamos la segunda
        rma.inspeccionar(producto2, true);
        assertThat(rma.getEstado()).isEqualTo(EstadoRma.INSPECCION_APROBADA);
        
        events = rma.pullDomainEvents();
        assertThat(events).hasSize(1);
        assertThat(events.get(0)).isInstanceOf(ProductoAprobadoParaReingresoEvent.class);
    }

    @Test
    void flujoParcialConRechazosGeneraEventosDuales() {
        var linea1 = new LineaDevolucion(producto1, CantidadDevuelta.de(2), MotivoDevolucion.de("Roto"));
        var linea2 = new LineaDevolucion(producto2, CantidadDevuelta.de(1), MotivoDevolucion.de("Caja abierta"));
        
        AutorizacionDevolucion rma = AutorizacionDevolucion.emitir(empresaId, devolucionId, documentoFuenteId, List.of(linea1, linea2));
        rma.recibirFisicamente();

        // Primera rechazada
        rma.inspeccionar(producto1, false);
        
        List<Object> events = rma.pullDomainEvents();
        assertThat(events).hasSize(1);
        assertThat(events.get(0)).isInstanceOf(ProductoRechazadoAMermaEvent.class);

        // Segunda aprobada
        rma.inspeccionar(producto2, true);
        
        events = rma.pullDomainEvents();
        assertThat(events).hasSize(1);
        assertThat(events.get(0)).isInstanceOf(ProductoAprobadoParaReingresoEvent.class);
        
        assertThat(rma.getEstado()).isEqualTo(EstadoRma.INSPECCION_PARCIAL);
    }

    @Test
    void dobleInspeccionSobreMismaLineaLanzaExcepcion() {
        var linea = new LineaDevolucion(producto1, CantidadDevuelta.de(1), MotivoDevolucion.de("Defectuoso"));
        AutorizacionDevolucion rma = AutorizacionDevolucion.emitir(empresaId, devolucionId, documentoFuenteId, List.of(linea));
        rma.recibirFisicamente();
        
        rma.inspeccionar(producto1, true);
        
        assertThatThrownBy(() -> rma.inspeccionar(producto1, false))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("La línea ya fue inspeccionada");
    }
}
