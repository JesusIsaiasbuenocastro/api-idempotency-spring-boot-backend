package com.jesus.portafolio.idempotency.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.jesus.portafolio.idempotency.infrastructure.adapter.in.web.interceptor.IdempotencyInterceptor;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer{

    private final IdempotencyInterceptor interceptor;

    public WebMvcConfig(IdempotencyInterceptor interceptor) {
        this.interceptor = interceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(interceptor)
                .addPathPatterns("/api/**");
    }

}
