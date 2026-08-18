package com.SITFAI_CORE_ERP_TIENDA.iam_module.infrastructure.adapter.out.identity;

import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.event.UsuarioRegistradoEvent;
import jakarta.ws.rs.core.Response;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Driven Adapter: Escucha eventos de dominio UsuarioRegistradoEvent y sincroniza la identidad en Keycloak.
 * Cumple con MT-06 (inyección de empresa_id en atributos de usuario y asignación de rol).
 */
@Component
public class KeycloakUsuarioSyncListener {

    private static final Logger log = LoggerFactory.getLogger(KeycloakUsuarioSyncListener.class);

    private final Keycloak keycloak;
    private final String targetRealm;

    public KeycloakUsuarioSyncListener(
            Keycloak keycloak,
            @Value("${keycloak.admin.realm:sitfai-erp}") String targetRealm
    ) {
        this.keycloak = Objects.requireNonNull(keycloak, "keycloak client no puede ser null.");
        this.targetRealm = Objects.requireNonNull(targetRealm, "targetRealm no puede ser null.");
    }

    @EventListener
    public void onUsuarioRegistrado(UsuarioRegistradoEvent event) {
        if (event == null) {
            return;
        }

        log.info("Iniciando sincronización de usuario hacia Keycloak: username={}, empresaId={}, rol={}",
                event.username(), event.empresaId().valor(), event.rol());

        try {
            RealmResource realmResource = keycloak.realm(targetRealm);
            UsersResource usersResource = realmResource.users();

            // 1. Configurar UserRepresentation
            UserRepresentation user = new UserRepresentation();
            user.setUsername(event.username());
            user.setEmail(event.email());
            user.setEnabled(true);
            user.setEmailVerified(true);

            // 2. MT-06: Inyección de empresa_id como atributo del usuario
            user.setAttributes(Map.of(
                    "empresa_id", List.of(event.empresaId().valor().toString())
            ));

            // 3. Credencial temporal inicial (password = username, temporary = true)
            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setValue(event.username());
            credential.setTemporary(true);
            user.setCredentials(List.of(credential));

            // 4. Crear usuario en Keycloak
            String keycloakUserId = null;
            try (Response response = usersResource.create(user)) {
                if (response.getStatus() == Response.Status.CREATED.getStatusCode()) {
                    keycloakUserId = CreatedResponseUtil.getCreatedId(response);
                    log.info("Usuario '{}' creado exitosamente en Keycloak con ID: {}", event.username(), keycloakUserId);
                } else if (response.getStatus() == Response.Status.CONFLICT.getStatusCode()) {
                    log.warn("El usuario '{}' ya existe en Keycloak (HTTP 409 Conflict). Obteniendo ID existente...", event.username());
                    List<UserRepresentation> existingUsers = usersResource.searchByUsername(event.username(), true);
                    if (!existingUsers.isEmpty()) {
                        keycloakUserId = existingUsers.get(0).getId();
                    }
                } else {
                    log.error("Error al crear usuario en Keycloak. Status: {}, Info: {}",
                            response.getStatus(), response.getStatusInfo());
                    return;
                }
            }

            if (keycloakUserId == null) {
                log.error("No se pudo obtener el ID de Keycloak para el usuario: {}", event.username());
                return;
            }

            // 5. Asignar Rol de Realm al Usuario
            if (event.rol() != null && !event.rol().isBlank()) {
                asignarRolRealm(realmResource, keycloakUserId, event.rol());
            }

            log.info("Sincronización con Keycloak completada exitosamente para: {}", event.username());

        } catch (Exception e) {
            log.error("Fallo durante la sincronización del usuario '{}' con Keycloak: {}",
                    event.username(), e.getMessage(), e);
        }
    }

    private void asignarRolRealm(RealmResource realmResource, String keycloakUserId, String rolNombre) {
        try {
            RoleRepresentation roleRep = realmResource.roles().get(rolNombre).toRepresentation();
            if (roleRep != null) {
                UserResource userResource = realmResource.users().get(keycloakUserId);
                userResource.roles().realmLevel().add(Collections.singletonList(roleRep));
                log.info("Rol '{}' asignado a usuario '{}' en Keycloak.", rolNombre, keycloakUserId);
            }
        } catch (Exception e) {
            log.warn("No se pudo asignar el rol '{}' en Keycloak: {}", rolNombre, e.getMessage());
        }
    }
}
