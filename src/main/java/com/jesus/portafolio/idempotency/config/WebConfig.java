package com.jesus.portafolio.idempotency.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.jesus.portafolio.idempotency.interceptor.IdempotencyInterceptor;

@Configuration
public class WebConfig implements WebMvcConfigurer{

     private final IdempotencyInterceptor interceptor;

    public WebConfig(IdempotencyInterceptor interceptor) {
        this.interceptor = interceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(interceptor)
                .addPathPatterns("/api/**");
    }

    

    

}
