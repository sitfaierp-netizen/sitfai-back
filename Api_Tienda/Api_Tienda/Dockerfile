# =============================================================================
# SITFAI ERP — Backend Dockerfile (Api_Tienda / Modular Monolith)
# Stack: Java 21 | Spring Boot 3.4.0 | Eclipse Temurin 21 JRE Alpine
# =============================================================================

# --- Etapa 1: Build y Empaquetado con Maven ---
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /workspace

# Copiar configuración de Maven para resolución de dependencias en capa cacheada
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x ./mvnw
RUN ./mvnw dependency:go-offline -B || true

# Copiar código fuente y compilar artefacto JAR
COPY src ./src
RUN ./mvnw clean package -DskipTests

# --- Etapa 2: Imagen Ligera de Ejecución (Runtime) ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Usuario no privilegiado para seguridad en producción
RUN addgroup -S sitfai && adduser -S sitfai -G sitfai

# Copiar el ejecutable generado desde la etapa de compilación
COPY --from=builder /workspace/target/*.jar app.jar
RUN chown -R sitfai:sitfai /app

USER sitfai:sitfai

# Exponer el puerto oficial del Bounded Context (ADR-006)
EXPOSE 8084

# Configuración del ENTRYPOINT
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
