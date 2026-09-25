package com.SITFAI_CORE_ERP_TIENDA.returns.domain.model.rma.event;

import com.SITFAI_CORE_ERP_TIENDA.returns.domain.model.rma.vo.CantidadDevuelta;
import com.SITFAI_CORE_ERP_TIENDA.returns.domain.model.rma.vo.DevolucionId;
import com.SITFAI_CORE_ERP_TIENDA.returns.domain.model.rma.vo.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.returns.domain.model.rma.vo.MotivoDevolucion;
import com.SITFAI_CORE_ERP_TIENDA.returns.domain.model.rma.vo.ProductoId;

public record ProductoRechazadoAMermaEvent(
        EmpresaId empresaId,
        DevolucionId devolucionId,
        ProductoId productoId,
        CantidadDevuelta cantidad,
        MotivoDevolucion motivo
) {}
