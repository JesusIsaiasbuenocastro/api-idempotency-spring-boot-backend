package com.jesus.portafolio.idempotency.application.port.out;


public final class IdempotencyDecision {

    public enum Type { PROCEED, CACHED }
    private final Type type;
    private final IdempotentPayload cachedPayload;
    public IdempotencyDecision(Type type, IdempotentPayload cachedPayload) {
        this.type = type;
        this.cachedPayload = cachedPayload;
    }
    public static IdempotencyDecision cached(IdempotentPayload payload){
        return new IdempotencyDecision(Type.CACHED, payload);
    }

    public static IdempotencyDecision proceed(){
        return new IdempotencyDecision(Type.PROCEED, null);
    }

    public boolean isCached(){
        return type == Type.CACHED;
    }
    public IdempotentPayload cachedPayload(){
        return cachedPayload;
    }
}
