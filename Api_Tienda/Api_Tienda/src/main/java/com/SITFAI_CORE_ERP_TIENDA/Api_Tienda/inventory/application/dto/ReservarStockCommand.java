package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.DocumentoFuenteId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.ProductoId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.Cantidad;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

public record ReservarStockCommand(
        UUID empresaId,
        UUID productoId,
        BigDecimal cantidad,
        String referenciaOrigen
) {
    public ReservarStockCommand {
        Objects.requireNonNull(empresaId, "empresaId es obligatorio");
        Objects.requireNonNull(productoId, "productoId es obligatorio");
        Objects.requireNonNull(cantidad, "cantidad es obligatoria");
        Objects.requireNonNull(referenciaOrigen, "referenciaOrigen es obligatoria");
        if (cantidad.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
        }
    }

    public EmpresaId toEmpresaId() {
        return new EmpresaId(empresaId);
    }

    public ProductoId toProductoId() {
        return new ProductoId(productoId);
    }

    public Cantidad toCantidad() {
        return Cantidad.de(cantidad);
    }

    public DocumentoFuenteId toDocumentoFuenteId() {
        return new DocumentoFuenteId("PEDIDO_ECOMMERCE", referenciaOrigen);
    }
}
