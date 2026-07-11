package com.jesus.portafolio.idempotency.infrastructure.adapter.in.web.exception;

public class MissingIdempotencyKeyException extends RuntimeException{
    public MissingIdempotencyKeyException(String message){
        super(message);
    }
}
