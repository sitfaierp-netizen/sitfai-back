package com.SITFAI_CORE_ERP_TIENDA.replenishment.domain.model.politica;

import com.SITFAI_CORE_ERP_TIENDA.replenishment.domain.event.NecesidadAbastecimientoDetectadaEvent;
import com.SITFAI_CORE_ERP_TIENDA.replenishment.domain.model.politica.vo.BodegaId;
import com.SITFAI_CORE_ERP_TIENDA.replenishment.domain.model.politica.vo.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.replenishment.domain.model.politica.vo.NivelOptimo;
import com.SITFAI_CORE_ERP_TIENDA.replenishment.domain.model.politica.vo.PoliticaId;
import com.SITFAI_CORE_ERP_TIENDA.replenishment.domain.model.politica.vo.ProductoId;
import com.SITFAI_CORE_ERP_TIENDA.replenishment.domain.model.politica.vo.PuntoReorden;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class PoliticaInventario {

    private final PoliticaId id;
    private final EmpresaId empresaId;
    private final BodegaId bodegaId;
    private final ProductoId productoId;
    
    private PuntoReorden puntoReorden;
    private NivelOptimo nivelOptimo;
    private boolean activa;

    private final List<Object> domainEvents = new ArrayList<>();

    public PoliticaInventario(PoliticaId id, EmpresaId empresaId, BodegaId bodegaId, ProductoId productoId, 
                              PuntoReorden puntoReorden, NivelOptimo nivelOptimo, boolean activa) {
        this.id = Objects.requireNonNull(id, "PoliticaId no puede ser nulo");
        this.empresaId = Objects.requireNonNull(empresaId, "EmpresaId no puede ser nulo (MT-01)");
        this.bodegaId = Objects.requireNonNull(bodegaId, "BodegaId no puede ser nulo");
        this.productoId = Objects.requireNonNull(productoId, "ProductoId no puede ser nulo");
        
        Objects.requireNonNull(puntoReorden, "PuntoReorden no puede ser nulo");
        Objects.requireNonNull(nivelOptimo, "NivelOptimo no puede ser nulo");
        
        if (nivelOptimo.valor() <= puntoReorden.valor()) {
            throw new IllegalArgumentException("El Nivel Óptimo debe ser estricatamente mayor al Punto de Reorden");
        }

        this.puntoReorden = puntoReorden;
        this.nivelOptimo = nivelOptimo;
        this.activa = activa;
    }

    public void evaluarStock(int stockDisponible) {
        if (!activa) return;

        if (stockDisponible <= puntoReorden.valor()) {
            int cantidadAReponer = nivelOptimo.valor() - stockDisponible;
            
            domainEvents.add(new NecesidadAbastecimientoDetectadaEvent(
                    UUID.randomUUID(),
                    Instant.now(),
                    this.empresaId.valor(),
                    this.bodegaId.valor(),
                    this.productoId.valor(),
                    cantidadAReponer
            ));
        }
    }

    public PoliticaId getId() { return id; }
    public EmpresaId getEmpresaId() { return empresaId; }
    public BodegaId getBodegaId() { return bodegaId; }
    public ProductoId getProductoId() { return productoId; }
    public PuntoReorden getPuntoReorden() { return puntoReorden; }
    public NivelOptimo getNivelOptimo() { return nivelOptimo; }
    public boolean isActiva() { return activa; }

    public List<Object> pullDomainEvents() {
        List<Object> events = new ArrayList<>(domainEvents);
        domainEvents.clear();
        return Collections.unmodifiableList(events);
    }
}
