package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.domain.model.bom;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.domain.model.bom.vo.CantidadInsumo;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.domain.model.bom.vo.InsumoId;

import java.util.Objects;
import java.util.UUID;

public class ComponenteReceta {
    private final UUID id;
    private final InsumoId insumoId;
    private final CantidadInsumo cantidad;

    public ComponenteReceta(InsumoId insumoId, CantidadInsumo cantidad) {
        this(UUID.randomUUID(), insumoId, cantidad);
    }

    public ComponenteReceta(UUID id, InsumoId insumoId, CantidadInsumo cantidad) {
        this.id = Objects.requireNonNull(id, "El id del componente no puede ser nulo");
        this.insumoId = Objects.requireNonNull(insumoId, "El insumoId no puede ser nulo");
        this.cantidad = Objects.requireNonNull(cantidad, "La cantidad no puede ser nula");
    }

    public UUID getId() {
        return id;
    }

    public InsumoId getInsumoId() {
        return insumoId;
    }

    public CantidadInsumo getCantidad() {
        return cantidad;
    }
}
