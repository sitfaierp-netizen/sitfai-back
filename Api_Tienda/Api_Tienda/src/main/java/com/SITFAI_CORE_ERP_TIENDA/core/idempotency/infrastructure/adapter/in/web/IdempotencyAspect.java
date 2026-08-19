package com.SITFAI_CORE_ERP_TIENDA.core.idempotency.infrastructure.adapter.in.web;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.port.output.TenantProviderPort;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.core.idempotency.application.service.IdempotencyManagerService;
import com.SITFAI_CORE_ERP_TIENDA.core.idempotency.domain.model.IdempotencyKey;
import com.SITFAI_CORE_ERP_TIENDA.core.idempotency.domain.model.IdempotencyRecord;
import com.SITFAI_CORE_ERP_TIENDA.core.idempotency.domain.model.PayloadFingerprint;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Aspect
@Component
public class IdempotencyAspect {

    private static final String IDEMPOTENCY_HEADER = "Idempotency-Key";
    private final IdempotencyManagerService idempotencyManagerService;
    private final TenantProviderPort tenantProviderPort;

    public IdempotencyAspect(IdempotencyManagerService idempotencyManagerService, TenantProviderPort tenantProviderPort) {
        this.idempotencyManagerService = idempotencyManagerService;
        this.tenantProviderPort = tenantProviderPort;
    }

    @Around("@annotation(com.SITFAI_CORE_ERP_TIENDA.core.idempotency.infrastructure.adapter.in.web.Idempotent)")
    public Object handleIdempotency(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = getHttpServletRequest();
        if (request == null) {
            return joinPoint.proceed();
        }

        String keyHeader = request.getHeader(IDEMPOTENCY_HEADER);
        if (keyHeader == null || keyHeader.isBlank()) {
            throw new IllegalArgumentException("Header Idempotency-Key es obligatorio para esta operación.");
        }

        EmpresaId empresaId = tenantProviderPort.getEmpresaIdAutenticada();
        IdempotencyKey key = new IdempotencyKey(keyHeader);
        PayloadFingerprint fingerprint = generateFingerprint(joinPoint.getArgs());

        IdempotencyRecord record = idempotencyManagerService.processRequest(empresaId, key, fingerprint);

        if (record.isCompleted()) {
            // Ya procesado, devolver respuesta cacheada
            return ResponseEntity.status(record.getHttpStatus()).body(record.getResponseBody());
        }

        try {
            Object result = joinPoint.proceed();
            
            // Si el método es de un controlador y devuelve ResponseEntity
            if (result instanceof ResponseEntity<?> responseEntity) {
                String bodyStr = responseEntity.getBody() != null ? responseEntity.getBody().toString() : "";
                idempotencyManagerService.completeRequest(empresaId, key, responseEntity.getStatusCode().value(), bodyStr);
            } else {
                idempotencyManagerService.completeRequest(empresaId, key, 200, result != null ? result.toString() : "");
            }
            return result;

        } catch (Exception e) {
            idempotencyManagerService.failRequest(empresaId, key, 500, e.getMessage());
            throw e;
        }
    }

    private HttpServletRequest getHttpServletRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    private PayloadFingerprint generateFingerprint(Object[] args) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            StringBuilder sb = new StringBuilder();
            for (Object arg : args) {
                if (arg != null && !(arg instanceof HttpServletRequest) && !(arg instanceof HttpServletResponse)) {
                    sb.append(arg.toString());
                }
            }
            byte[] hash = digest.digest(sb.toString().getBytes(StandardCharsets.UTF_8));
            return new PayloadFingerprint(Base64.getEncoder().encodeToString(hash));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error generando payload fingerprint", e);
        }
    }
}
