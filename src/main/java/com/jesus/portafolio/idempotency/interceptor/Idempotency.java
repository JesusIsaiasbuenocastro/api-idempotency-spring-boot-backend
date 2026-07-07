package com.jesus.portafolio.idempotency.interceptor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface  Idempotency {
    //Tiempo de expiración ttl
    long ttlSeconds() default 86400; 
}
