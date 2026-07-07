package com.jesus.portafolio.idempotency.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/v1/payment")
public class IdempotencyController {

    @PostMapping
    public String postMethodName(@RequestBody String entity) {
        
        return entity;
    }
    

}
