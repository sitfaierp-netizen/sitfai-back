# =============================================================================
# SITFAI ERP — Backend Dockerfile (Contexto: Api_Tienda)
# Stack: Java 25 | Spring Boot 4.0.7 | Eclipse Temurin 25 JRE Alpine
# =============================================================================

FROM eclipse-temurin:25-jdk-alpine AS builder
WORKDIR /workspace

COPY Api_Tienda/.mvn/ .mvn/
COPY Api_Tienda/mvnw Api_Tienda/pom.xml ./
RUN chmod +x ./mvnw
RUN ./mvnw dependency:go-offline -B || true

COPY Api_Tienda/src ./src
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

RUN addgroup -S sitfai && adduser -S sitfai -G sitfai

COPY --from=builder /workspace/target/*.jar app.jar
RUN chown -R sitfai:sitfai /app

USER sitfai:sitfai

EXPOSE 8084

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
