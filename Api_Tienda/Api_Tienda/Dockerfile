# =============================================================================
# SITFAI ERP — Backend Dockerfile (Api_Tienda / Modular Monolith)
# Stack: Java 21 | Spring Boot 3.4.0 | Eclipse Temurin 21 JRE Alpine
# =============================================================================

# --- Etapa 2: Imagen Ligera de Ejecución (Runtime) ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Usuario no privilegiado para seguridad en producción
RUN addgroup -S sitfai && adduser -S sitfai -G sitfai

# Copiar el ejecutable generado en el host
COPY target/*.jar app.jar
RUN chown -R sitfai:sitfai /app

USER sitfai:sitfai

# Exponer el puerto oficial del Bounded Context (ADR-006)
EXPOSE 8084

# Configuración del ENTRYPOINT
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
