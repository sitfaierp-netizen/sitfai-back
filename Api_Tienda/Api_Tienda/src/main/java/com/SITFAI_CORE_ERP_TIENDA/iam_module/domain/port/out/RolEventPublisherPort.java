package com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.port.out;

import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.event.RolCreadoEvent;

public interface RolEventPublisherPort {
    void publicar(RolCreadoEvent event);
}
