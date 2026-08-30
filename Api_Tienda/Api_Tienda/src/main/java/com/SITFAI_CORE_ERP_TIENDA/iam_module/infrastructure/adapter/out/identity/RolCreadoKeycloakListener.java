package com.SITFAI_CORE_ERP_TIENDA.iam_module.infrastructure.adapter.out.identity;

import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.event.RolCreadoEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class RolCreadoKeycloakListener {

    private static final Logger log = LoggerFactory.getLogger(RolCreadoKeycloakListener.class);

    // En un escenario real, inyectaríamos el KeycloakAdminClient aquí
    // private final Keycloak keycloakAdminClient;

    @EventListener
    public void onRolCreado(RolCreadoEvent event) {
        log.info("Sincronizando rol con Keycloak. Código: {}, Nombre: {}", event.getCodigo(), event.getNombre());
        // Lógica para crear el Realm Role en Keycloak:
        // RoleRepresentation role = new RoleRepresentation();
        // role.setName(event.getNombre());
        // role.setDescription("Generado automáticamente desde SITFAI ERP");
        // keycloakAdminClient.realm("sitfai-realm").roles().create(role);
        log.info("Rol {} sincronizado exitosamente con Keycloak (Mocked).", event.getNombre());
    }
}
