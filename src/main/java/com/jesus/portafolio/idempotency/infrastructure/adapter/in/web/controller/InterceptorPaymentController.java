package com.jesus.portafolio.idempotency.infrastructure.adapter.in.web.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jesus.portafolio.idempotency.application.annotation.Idempotency;



@RestController
@RequestMapping("/api/v1/payment")
public class InterceptorPaymentController {

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
