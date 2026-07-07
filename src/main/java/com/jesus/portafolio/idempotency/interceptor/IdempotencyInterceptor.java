package com.jesus.portafolio.idempotency.interceptor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.ContentCachingRequestWrapper;

import com.jesus.portafolio.idempotency.exceptions.MissingIdempotencyKeyException;
import com.jesus.portafolio.idempotency.service.IdempotencyService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class IdempotencyInterceptor implements HandlerInterceptor {
    
    
   private final IdempotencyService service;

   private static final String HEADER_NAME = "x-idempotency-Key";

    public IdempotencyInterceptor(IdempotencyService service) {
        this.service = service;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
                
                //Validación del metodo a aplicar la idempotencia
                if(!(handler instanceof HandlerMethod)){
                    return true;
                }

                HandlerMethod handlerMethod = (HandlerMethod) handler;
                Idempotency annotation = handlerMethod.getMethodAnnotation(Idempotency.class);
                
                if(annotation == null){
                    return true;
                }

                //Validación del header

                String key = request.getHeader(HEADER_NAME);
                if (key == null || key.isBlank()) {
                    throw new MissingIdempotencyKeyException(
                            "Este endpoint requiere la cabecera '" + HEADER_NAME + "' para evitar operaciones duplicadas.");
                }

                //hash del body 
                String body =    extractBody(request);  

                //validacion de la cache con el header

            


		return true;
	}

    private String extractBody(HttpServletRequest request) throws IOException {
        if (request instanceof ContentCachingRequestWrapper wrapper) {
            // En preHandle el body todavía no fue leído por Spring MVC, así
            // que forzamos la lectura completa para poblar el buffer interno.
            byte[] buf = StreamUtils.copyToByteArray(wrapper.getInputStream());
            return new String(buf, StandardCharsets.UTF_8);
        }
        return "";
    }

}
