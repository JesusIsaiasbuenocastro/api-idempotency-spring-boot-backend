package com.jesus.portafolio.idempotency.application.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface IdempotencyOperation {

    //Tiempo de expiración ttl
    long ttlSeconds() default 86400; 
}
