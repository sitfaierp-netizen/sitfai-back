# Phase 0B-S — Security train upgrade

Date: 2026-09-30  
Base: `develop@ff1ba0a04c05969b7073f30e3e1140f24a3a26cd`  
Branch: `fix/phase0-security-train-upgrade`  
Classification: `REQUIRED_COMPATIBILITY_CHANGE`

## Result

`SECURITY_TRAIN_PASS`

SITFAI now uses the latest stable Spring Boot release available at the time of this work, `4.1.1`, on Java 21. The application, test stack, Flyway integration, Jackson integration, OpenAPI integration and Testcontainers coordinates were migrated as one compatible runtime train. No IAM behavior, business rule, tenant model, migration or authorization policy was redesigned.

Official version sources checked on 2026-09-30:

- Spring Boot stable releases: <https://github.com/spring-projects/spring-boot/releases>
- Spring Boot supported lines: <https://github.com/spring-projects/spring-boot/wiki/Supported-Versions>
- Spring Boot 4 migration guide: <https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Migration-Guide>
- Springdoc Boot 4 compatibility: <https://github.com/springdoc/springdoc-openapi>
- MySQL Connector/J production recommendation: <https://dev.mysql.com/doc/connector-j/en/>
- Tomcat 11 advisories: <https://tomcat.apache.org/security-11.html>

Boot `4.1.2` was still a snapshot and Boot `4.2` was a milestone; neither was eligible. Boot `4.1.1` and `4.0.8` both managed Tomcat `11.0.24`, so changing Boot lines would not remove the residual Tomcat findings.

## Runtime versions

| Component | Before | After | Management / rationale |
|---|---:|---:|---|
| Java | 21 | 21 | Canonical SITFAI runtime retained |
| Spring Boot | 3.4.0 | 4.1.1 | Latest stable supported 4.1.x |
| Spring Framework | 6.2.0 | 7.0.9 | Boot BOM |
| Spring Security | 6.4.1 | 7.1.1 | Boot BOM |
| Embedded Tomcat | 10.1.33 | 11.0.24 | Boot BOM; no direct pin |
| Jackson used by SITFAI | 2.18.1 | 3.1.5 | Boot BOM and Boot 4 native mapper |
| Jackson 2 compatibility subtree | 2.18.1 | 2.21.5 | Boot BOM; required transitively by Keycloak 26.0.0 |
| MySQL Connector/J | 9.1.0 | 26.7.0 | Direct runtime dependency; Oracle-recommended production line, tested with MySQL 8.4 |
| Flyway | 10.20.1 | 12.4.0 | Boot BOM and dedicated Boot 4 starter |
| Testcontainers | 1.20.4 explicit BOM | 2.0.5 Boot-managed | Boot BOM; current artifact coordinates |
| Springdoc | 2.7.0 | 3.0.2 | Required Boot 4 compatible major |
| Keycloak admin client | 26.0.0 | 26.0.0 | Retained; compiled and verified without functional changes |

`commons-io:2.20.0` is the only additional compatibility version. Keycloak 26.0.0 introduces `2.11.0` transitively, while Testcontainers 2.0.5/Commons Compress 1.28 uses the `FileTimes` API from Commons IO 2.20. This override fixes a demonstrated runtime linkage failure and is not a Spring/Tomcat/Jackson train pin.

## Required compatibility changes

- Renamed Boot 4 starters for MVC and OAuth2 resource server.
- Replaced direct `flyway-core` with `spring-boot-starter-flyway`; retained `flyway-mysql` and every V1–V59 migration unchanged.
- Added the modular Boot 4 MVC/JPA test starters.
- Removed the explicit Testcontainers 1.20.4 BOM and adopted Boot-managed Testcontainers 2.0.5 coordinates.
- Moved Boot annotations (`EntityScan`, MVC test annotations, JPA test annotations and validation auto-configuration) to their Boot 4 packages.
- Continued the completed Mockito migration with Spring Framework `MockitoBean`; removed the obsolete Boot `SpyBean` import.
- Migrated SITFAI serialization code and tests from Jackson 2 packages to Boot 4's native Jackson 3 `JsonMapper` APIs. No default polymorphic typing was enabled.
- Filtered Spring/Hibernate/Apache lifecycle events at `@EventListener` dispatch time. Spring Framework 7 pauses cached test contexts; filtering after entering the transactional proxy attempted to open a connection to an already stopped Testcontainer. Domain-event semantics are unchanged.

All changes above are classified `REQUIRED_COMPATIBILITY_CHANGE`. No `UNRELATED_CHANGE` is included.

## Verification

Final command:

```text
./mvnw --batch-mode --no-transfer-progress clean verify
```

Result: `BUILD SUCCESS` in 4m53s on Temurin `21.0.12.1`.

| Gate | Result |
|---|---:|
| Surefire | 348 passed; 0 failures; 0 errors; 4 pre-existing skipped |
| Failsafe | 40 passed; 0 failures; 0 errors; 0 skipped |
| Total | 388 |
| Domain JaCoCo instruction coverage | 66.2153% (13,790 covered / 20,826 total) |
| JaCoCo blocking baseline | 64% — passed |
| MySQL Testcontainers | `mysql:8.4.0` — passed |
| Flyway | fresh schema through V59; Hibernate `ddl-auto=validate` passed |
| Application context | passed |
| Tenant signed-JWT E2E | `TenantIsolationIntegrationTest` passed |
| JWT role conversion | 6 tests passed |
| Tenant provider | 4 tests passed |
| MVC role/method-security coverage | 7 tests passed |
| Dependency-Check 12.1.0 | executed; report generated; no suppressions |

The production source set (987 files) and test source set (89 files) both compile on the final train.

## Dependency-Check comparison

Counts below are report occurrences, matching the pre-upgrade counting method.

| Severity | Before | After |
|---|---:|---:|
| CRITICAL | 27 | 4 |
| HIGH | 46 | 5 |
| MEDIUM | 69 | 42 |
| LOW | 6 | 0 |
| Total | 148 | 51 |
| Vulnerable dependencies | not recorded in baseline summary | 5 |
| Suppressed | 0 | 0 |

| Applicability | Before | After |
|---|---:|---:|
| Runtime-applicable CRITICAL | 1 | 0 |
| Runtime-applicable HIGH | 2 | 0 |

The final report contains 32 unique CVE identifiers. Its nine unique CRITICAL/HIGH findings all map to Boot-managed Tomcat `11.0.24`:

- CRITICAL: `CVE-2026-65182`, `CVE-2026-65637`, `CVE-2026-65905`, `CVE-2026-68525`.
- HIGH: `CVE-2026-65183`, `CVE-2026-65927`, `CVE-2026-66422`, `CVE-2026-68569`, `CVE-2026-68763`.

They require, respectively, container declarative security constraints, strict SNI/CLIENT_CERT conditions, DIGEST authentication, FORM authentication, Unix-domain sockets, RewriteValve rules, Tomcat Realm role aliases, CLIENT_CERT/SPNEGO with DataSourceRealm, or HTTP/2. Repository and runtime configuration searches demonstrate that SITFAI uses Spring Security bearer JWT, no `web.xml` constraints, no Tomcat Realm/authenticator, no RewriteValve, no Unix-domain socket and no HTTP/2. Therefore none is applicable to the final runtime configuration.

Apache fixes these findings in Tomcat 11.0.25 or later (11.0.26 was current), but the latest stable Boot 4.1.1 BOM still manages 11.0.24. A direct Tomcat pin is deliberately not introduced. Upgrade when a stable Spring Boot BOM carries the fixed Tomcat patch.

## Required CVE decisions

| CVE | Before | Final decision | Evidence |
|---|---|---|---|
| `CVE-2026-22732` | Applicable CRITICAL; Security 6.4.1 | `FIXED` | Spring Security 7.1.1; CVE absent from final report; active JWT/security tests pass |
| `CVE-2025-31650` | Applicable HIGH; Tomcat 10.1.33 | `FIXED` | Tomcat 11.0.24 is outside the affected release range; CVE absent |
| `CVE-2025-30706` | Applicable HIGH; Connector/J 9.1.0 | `FIXED` | Connector/J 26.7.0; CVE absent; MySQL 8.4 integration passes |

No new runtime-applicable CRITICAL was introduced.

## Secret defaults deferred to Identity Block B

No secret value is reproduced here.

| File | Configuration type | Risk | Runtime reachability |
|---|---|---|---|
| `Api_Tienda/Api_Tienda/src/main/resources/application.properties` | Database password environment placeholder with an insecure fallback | Predictable credential if the environment variable is omitted | Yes, datasource startup |
| `Api_Tienda/Api_Tienda/src/main/resources/application.properties` | Keycloak administrative username/password placeholders with insecure fallbacks | Administrative credential exposure or unintended default use | Yes, when Keycloak admin client operations are invoked |
| `Api_Tienda/Api_Tienda/src/main/java/com/SITFAI_CORE_ERP_TIENDA/iam_module/infrastructure/adapter/out/identity/KeycloakClientConfig.java` | Additional password property fallback | Same administrative credential risk and duplicated default source | Yes, bean construction / admin-client use |

Removal and fail-fast validation belong to the subsequent Identity hardening block and were intentionally not mixed into this runtime upgrade.

## Residual risks

- Boot-managed Tomcat 11.0.24 has nine scanner CRITICAL/HIGH findings that are not applicable to the current SITFAI configuration. Track Boot 4.1.2 or a later stable BOM that carries Tomcat 11.0.25+; do not enable the affected Tomcat features beforehand.
- Keycloak admin client remains at 26.0.0 and retains a BOM-managed Jackson 2 compatibility subtree. Functional Keycloak hardening remains blocked until this PR is reviewed and merged.
- Dependency-Check data is time-sensitive. The final report used NVD data last modified `2026-10-01T03:16:59Z` and must be regenerated in CI.
- Credential defaults remain a known Identity Block B risk.

