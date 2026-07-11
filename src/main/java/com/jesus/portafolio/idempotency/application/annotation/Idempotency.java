package com.jesus.portafolio.idempotency.application.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface  Idempotency {

    String headerKey() default "x-idempotency-key";
    //Tiempo de expiración ttl
    long ttlSeconds() default 86400; 
}
