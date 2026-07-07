package com.jesus.portafolio.idempotency.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jesus.portafolio.idempotency.interceptor.Idempotency;



@RestController
@RequestMapping("/api/v1/payment")
public class IdempotencyController {

    @PostMapping
    @Idempotency(ttlSeconds=86400)
    public String postMethodName(@RequestBody String entity) {
        
        return entity;
    }

     @GetMapping
     public String getMethodName() {
         return "Consultar todos";
     }
      

}
