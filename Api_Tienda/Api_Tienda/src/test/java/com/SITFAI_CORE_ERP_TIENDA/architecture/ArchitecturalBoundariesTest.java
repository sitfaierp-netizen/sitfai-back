package com.SITFAI_CORE_ERP_TIENDA.architecture;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Source-level architecture checks for the package topology of this modular monolith.
 * These checks intentionally protect only boundaries that are already valid across all
 * bounded contexts; they do not force contexts to share their domain vocabulary.
 */
class ArchitecturalBoundariesTest {

    private static final Path MAIN_JAVA = Path.of("src", "main", "java");

    @Test
    void domainDoesNotImportSpring() throws IOException {
        assertNoMatchingImport(
                path -> normalized(path).contains("/domain/"),
                line -> line.startsWith("import org.springframework."),
                "Domain must remain framework-independent"
        );
    }

    @Test
    void domainDoesNotImportInfrastructure() throws IOException {
        assertNoMatchingImport(
                path -> normalized(path).contains("/domain/"),
                line -> line.startsWith("import ") && line.contains(".infrastructure."),
                "Domain must not depend on infrastructure"
        );
    }

    @Test
    void domainDoesNotImportApplication() throws IOException {
        assertNoMatchingImport(
                path -> normalized(path).contains("/domain/"),
                line -> line.startsWith("import ") && line.contains(".application."),
                "Domain must not depend on application DTOs or use cases"
        );
    }

    @Test
    void applicationDoesNotImportInfrastructure() throws IOException {
        assertNoMatchingImport(
                path -> normalized(path).contains("/application/"),
                line -> line.startsWith("import ") && line.contains(".infrastructure."),
                "Application must depend on ports, not concrete infrastructure"
        );
    }

    @Test
    void purchasingApplicationDoesNotImportSpringSecurity() throws IOException {
        assertNoMatchingImport(
                path -> normalized(path).contains("/purchasing/application/"),
                line -> line.startsWith("import org.springframework.security."),
                "Purchasing application must not resolve tenant trust from Spring Security"
        );
    }

    @Test
    void webControllersDoNotImportRepositories() throws IOException {
        assertNoMatchingImport(
                path -> normalized(path).contains("/infrastructure/adapter/in/web/")
                        && path.getFileName().toString().endsWith("Controller.java"),
                line -> line.startsWith("import ") && line.endsWith("Repository;"),
                "Web controllers must call input ports rather than repositories"
        );
    }

    @Test
    void coreIdempotencyDoesNotDependOnInventory() throws IOException {
        assertNoMatchingImport(
                path -> normalized(path).contains("/core/idempotency/"),
                line -> line.startsWith("import ") && line.contains(".Api_Tienda.inventory."),
                "Core idempotency must not depend on the Inventory bounded context"
        );
    }

    @Test
    void coreAuditHasOneCanonicalEventDispatcherPort() throws IOException {
        List<Path> ports;
        try (Stream<Path> files = Files.walk(MAIN_JAVA)) {
            ports = files.filter(Files::isRegularFile)
                    .filter(path -> normalized(path).contains("/core_audit/"))
                    .filter(path -> path.getFileName().toString().equals("EventDispatcherPort.java"))
                    .toList();
        }

        assertEquals(1, ports.size(), "core_audit must expose one EventDispatcherPort");
        assertTrue(normalized(ports.getFirst()).contains("/domain/port/output/"));
    }

    @Test
    void fulfillmentKeepsOnlyTheActiveDispatchAggregateFamily() {
        Path fulfillment = MAIN_JAVA.resolve(Path.of(
                "com", "SITFAI_CORE_ERP_TIENDA", "fulfillment", "domain", "model"));

        assertFalse(Files.exists(fulfillment.resolve("OrdenDespacho.java")),
                "The unused parallel aggregate must not return");
        assertTrue(Files.exists(fulfillment.resolve(Path.of("despacho", "OrdenDespacho.java"))),
                "The persisted and API-backed aggregate is canonical");
    }

    private void assertNoMatchingImport(
            Predicate<Path> sourceSelector,
            Predicate<String> forbiddenImport,
            String message) throws IOException {
        List<String> violations;
        try (Stream<Path> files = Files.walk(MAIN_JAVA)) {
            violations = files.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".java"))
                    .filter(sourceSelector)
                    .flatMap(path -> importViolations(path, forbiddenImport))
                    .toList();
        }
        assertTrue(violations.isEmpty(), message + ": " + violations);
    }

    private Stream<String> importViolations(Path path, Predicate<String> forbiddenImport) {
        try {
            return Files.readAllLines(path).stream()
                    .map(String::trim)
                    .filter(forbiddenImport)
                    .map(line -> normalized(path) + " -> " + line);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not inspect " + path, exception);
        }
    }

    private static String normalized(Path path) {
        return path.toString().replace('\\', '/');
    }
}
