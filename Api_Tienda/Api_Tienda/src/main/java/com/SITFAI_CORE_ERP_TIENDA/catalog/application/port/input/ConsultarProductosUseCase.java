package com.SITFAI_CORE_ERP_TIENDA.catalog.application.port.input;

import com.SITFAI_CORE_ERP_TIENDA.catalog.application.dto.ProductoResponse;
import java.util.List;

public interface ConsultarProductosUseCase {
    org.springframework.data.domain.Page<ProductoResponse> listarProductos(String search, org.springframework.data.domain.Pageable pageable);
}
