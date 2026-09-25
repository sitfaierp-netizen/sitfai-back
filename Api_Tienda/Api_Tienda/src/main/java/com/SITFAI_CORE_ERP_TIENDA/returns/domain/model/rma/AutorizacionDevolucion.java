package com.SITFAI_CORE_ERP_TIENDA.returns.domain.model.rma;

import com.SITFAI_CORE_ERP_TIENDA.returns.domain.model.rma.event.ProductoAprobadoParaReingresoEvent;
import com.SITFAI_CORE_ERP_TIENDA.returns.domain.model.rma.event.ProductoRechazadoAMermaEvent;
import com.SITFAI_CORE_ERP_TIENDA.returns.domain.model.rma.vo.DevolucionId;
import com.SITFAI_CORE_ERP_TIENDA.returns.domain.model.rma.vo.DocumentoFuenteId;
import com.SITFAI_CORE_ERP_TIENDA.returns.domain.model.rma.vo.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.returns.domain.model.rma.vo.EstadoRma;
import com.SITFAI_CORE_ERP_TIENDA.returns.domain.model.rma.vo.ProductoId;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AutorizacionDevolucion {
    private final EmpresaId empresaId;
    private final DevolucionId devolucionId;
    private final DocumentoFuenteId documentoFuenteId;
    private EstadoRma estado;
    private final List<LineaDevolucion> lineas;
    private final List<Object> domainEvents;

    private AutorizacionDevolucion(EmpresaId empresaId, DevolucionId devolucionId, DocumentoFuenteId documentoFuenteId, List<LineaDevolucion> lineas) {
        if (empresaId == null || devolucionId == null || documentoFuenteId == null || lineas == null || lineas.isEmpty()) {
            throw new IllegalArgumentException("Parámetros inválidos para crear AutorizacionDevolucion");
        }
        this.empresaId = empresaId;
        this.devolucionId = devolucionId;
        this.documentoFuenteId = documentoFuenteId;
        this.estado = EstadoRma.AUTORIZADA;
        this.lineas = new ArrayList<>(lineas);
        this.domainEvents = new ArrayList<>();
    }

    public static AutorizacionDevolucion emitir(EmpresaId empresaId, DevolucionId devolucionId, DocumentoFuenteId documentoFuenteId, List<LineaDevolucion> lineas) {
        return new AutorizacionDevolucion(empresaId, devolucionId, documentoFuenteId, lineas);
    }

    public void recibirFisicamente() {
        if (this.estado != EstadoRma.AUTORIZADA) {
            throw new IllegalStateException("Solo se puede recibir físicamente un RMA en estado AUTORIZADA");
        }
        this.estado = EstadoRma.RECIBIDA_EN_CUARENTENA;
    }

    public void inspeccionar(ProductoId productoId, boolean aprobado) {
        if (this.estado != EstadoRma.RECIBIDA_EN_CUARENTENA && this.estado != EstadoRma.INSPECCION_PARCIAL) {
            throw new IllegalStateException("Solo se puede inspeccionar en estado RECIBIDA_EN_CUARENTENA o INSPECCION_PARCIAL");
        }

        LineaDevolucion linea = this.lineas.stream()
                .filter(l -> l.getProductoId().equals(productoId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado en esta devolución"));

        linea.inspeccionar(aprobado);

        if (aprobado) {
            domainEvents.add(new ProductoAprobadoParaReingresoEvent(this.empresaId, this.devolucionId, productoId, linea.getCantidadDevuelta()));
        } else {
            domainEvents.add(new ProductoRechazadoAMermaEvent(this.empresaId, this.devolucionId, productoId, linea.getCantidadDevuelta(), linea.getMotivoDevolucion()));
        }

        evaluarCierreInspeccion();
    }

    private void evaluarCierreInspeccion() {
        long pendientes = lineas.stream().filter(l -> l.getEstadoInspeccion() == LineaDevolucion.EstadoInspeccion.PENDIENTE).count();
        if (pendientes == 0) {
            boolean todosAprobados = lineas.stream().allMatch(l -> l.getEstadoInspeccion() == LineaDevolucion.EstadoInspeccion.APROBADA);
            boolean todosRechazados = lineas.stream().allMatch(l -> l.getEstadoInspeccion() == LineaDevolucion.EstadoInspeccion.RECHAZADA);
            
            if (todosAprobados) {
                this.estado = EstadoRma.INSPECCION_APROBADA;
            } else if (todosRechazados) {
                this.estado = EstadoRma.INSPECCION_RECHAZADA;
            } else {
                // If it's a mix of approved and rejected, it's partially approved.
                // Wait, the prompt says "INSPECCION_APROBADA e INSPECCION_RECHAZADA", let's just say if everything is processed, the RMA is done.
                // I added INSPECCION_PARCIAL earlier, let's keep it if some are pending, or mixed. 
                // Wait, if 0 pending and mixed results, is it just APROBADA partially? Let's default to INSPECCION_APROBADA if at least one is approved? No, let's use INSPECCION_PARCIAL for mixed.
                this.estado = EstadoRma.INSPECCION_PARCIAL;
            }
        } else {
            this.estado = EstadoRma.INSPECCION_PARCIAL;
        }
    }

    public List<Object> pullDomainEvents() {
        List<Object> events = new ArrayList<>(this.domainEvents);
        this.domainEvents.clear();
        return events;
    }

    public EmpresaId getEmpresaId() { return empresaId; }
    public DevolucionId getDevolucionId() { return devolucionId; }
    public DocumentoFuenteId getDocumentoFuenteId() { return documentoFuenteId; }
    public EstadoRma getEstado() { return estado; }
    public List<LineaDevolucion> getLineas() { return Collections.unmodifiableList(lineas); }
}
