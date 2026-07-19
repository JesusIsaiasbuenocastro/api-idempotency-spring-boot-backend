package com.jesus.portafolio.idempotency.infrastructure.adapter.in.aop.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jesus.portafolio.idempotency.application.annotation.Idempotency;
import com.jesus.portafolio.idempotency.application.annotation.IdempotencyOperation;
import com.jesus.portafolio.idempotency.application.port.out.IdempotencyDecision;
import com.jesus.portafolio.idempotency.application.port.out.IdempotencyStoragePort;
import com.jesus.portafolio.idempotency.application.port.out.IdempotentPayload;
import com.jesus.portafolio.idempotency.infrastructure.adapter.in.web.exception.MissingIdempotencyKeyException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

@Aspect
@Component
public class IdempotencyAspect {

    private static final Logger log = LoggerFactory.getLogger(IdempotencyAspect.class);

    private final IdempotencyStoragePort idempotencyStoragePort;
    private final ObjectMapper objectMapper;

    public IdempotencyAspect(IdempotencyStoragePort idempotencyStoragePort, ObjectMapper objectMapper) {
        this.idempotencyStoragePort = idempotencyStoragePort;
        this.objectMapper = objectMapper;
    }

    @Around("@annotation(idempotentOperation)")
    public Object aroundIdempotentOperation(ProceedingJoinPoint joinPoint, IdempotencyOperation idempotentOperation) throws Throwable {

        String key = extractRequestId(joinPoint.getArgs());

        if (key == null || key.isBlank()) {
            throw new MissingIdempotencyKeyException(
                    "Este método requiere un 'requestId' no vacío en el argumento para evitar operaciones duplicadas.");
        }

        String operationName = joinPoint.getSignature().toShortString();
        String hash = hashArguments(joinPoint.getArgs());

        IdempotencyDecision decision = idempotencyStoragePort.begin(key, hash, operationName, idempotentOperation.ttlSeconds());


        if (decision.isCached()) {
            log.info("IdempotentOperation '{}' repetida con el mismo requestId '{}': devolviendo resultado cacheado desde Redis",
                    operationName, key);
            return deserialize(decision.cachedPayload().body(), returnType(joinPoint));
        }

        try {
            Object result = joinPoint.proceed();
            String json = objectMapper.writeValueAsString(result);
            idempotencyStoragePort.complete(key, IdempotentPayload.ofJson(json), idempotentOperation.ttlSeconds());
            return result;
        } catch (Throwable ex) {
            idempotencyStoragePort.fail(key);
            throw ex;
        }
    }

        private String extractRequestId(Object[] args) {
        for (Object arg : args) {
            if (arg == null) {
                continue;
            }
            try {
                Method requestIdAccessor = arg.getClass().getMethod("requestId");
                Object value = requestIdAccessor.invoke(arg);
                if (value != null) {
                    return value.toString();
                }
            } catch (NoSuchMethodException ignored) {
                // este argumento no expone requestId(); se intenta con el siguiente
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("No se pudo leer 'requestId' del argumento " + arg.getClass(), e);
            }
        }
        return null;
    }

    private String hashArguments(Object[] args) {
        try {
            String raw = objectMapper.writeValueAsString(args);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo calcular el hash de los argumentos", e);
        }
    }

    private Class<?> returnType(ProceedingJoinPoint joinPoint) {
        return ((MethodSignature) joinPoint.getSignature()).getReturnType();
    }

    private Object deserialize(String json, Class<?> type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo deserializar el resultado cacheado", e);
        }
    }
}
