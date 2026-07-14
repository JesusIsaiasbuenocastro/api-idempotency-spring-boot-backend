package com.jesus.portafolio.idempotency.infrastructure.adapter.in.web.interceptor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

import com.jesus.portafolio.idempotency.application.port.out.IdempotencyDecision;
import com.jesus.portafolio.idempotency.application.port.out.IdempotencyStoragePort;
import com.jesus.portafolio.idempotency.application.port.out.IdempotentPayload;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.ContentCachingRequestWrapper;

import com.jesus.portafolio.idempotency.application.annotation.Idempotency;
import com.jesus.portafolio.idempotency.infrastructure.adapter.in.web.exception.MissingIdempotencyKeyException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.util.ContentCachingResponseWrapper;

@Component
public class IdempotencyInterceptor implements HandlerInterceptor {

    private static final String HEADER_NAME = "x-idempotency-key";
    private static final String ATTR_KEY = "idempotency.key";
    private static final String ATTR_TTL = "idempotency.ttl";

   private final IdempotencyStoragePort idempotencyStoragePort;

    public IdempotencyInterceptor(IdempotencyStoragePort idempotencyStoragePort) {
        this.idempotencyStoragePort = idempotencyStoragePort;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {

        //Validación del metodo a aplicar la idempotencia
        if(!(handler instanceof HandlerMethod handlerMethod)){
            return true;
        }

        Idempotency annotation = handlerMethod.getMethodAnnotation(Idempotency.class);

        if(annotation == null){
            return true;
        }

        //Validacion del header
        String key = request.getHeader(HEADER_NAME);
        if (key == null || key.isBlank()) {
            throw new MissingIdempotencyKeyException(
                    "Este endpoint requiere la cabecera '" + HEADER_NAME + "' para evitar operaciones duplicadas.");
        }

        //hash del body
        String body = (String) request.getAttribute("cachedBody");
        String hash = hashRequest(request.getMethod(), request.getRequestURI(), body);

        IdempotencyDecision idempotencyDecision = idempotencyStoragePort.begin(
                key,hash, request.getMethod()+ " " +request.getRequestURI(), annotation.ttlSeconds() );

        if(idempotencyDecision.isCached()){
            writeCachedResponse(response, idempotencyDecision.cachedPayload());
            return false; //No llega a consumir de nuevo el controller trunca el proceso
        }

        //Guarda el payload en la cache
        request.setAttribute(ATTR_KEY,key);
        request.setAttribute(ATTR_TTL,annotation.ttlSeconds());
		return true;
	}

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        String key = (String) request.getAttribute(ATTR_KEY);
        if (key == null) {
            return;
        }

        if (ex != null) {
            idempotencyStoragePort.fail(key);
            return;
        }

        if (response instanceof ContentCachingResponseWrapper wrapper) {
            String body = new String(wrapper.getContentAsByteArray(), StandardCharsets.UTF_8);
            long ttlSeconds = (long) request.getAttribute(ATTR_TTL);
            idempotencyStoragePort.complete(
                    key, new IdempotentPayload(wrapper.getStatus(), body, wrapper.getContentType()), ttlSeconds);
        }
    }

    private String hashRequest(String method, String path, String body) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        String raw = method + "|" + path + "|" + (body == null ? "" : body);
        byte[] hash = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(hash);
    }

    private void writeCachedResponse(HttpServletResponse response, IdempotentPayload cached) throws IOException {
        response.setStatus(cached.httpStatus() != null ? cached.httpStatus() : 200);
        if (cached.contentType() != null) {
            response.setContentType(cached.contentType());
        }
        response.setHeader("Idempotent-Replayed", "true");
        response.getWriter().write(cached.body() == null ? "" : cached.body());
    }

}
