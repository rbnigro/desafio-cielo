package com.cielo.flashbooking.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@Tag(name = "Health", description = "Verificacao de disponibilidade da API")
public class HealthController {
    @GetMapping("/health")
    @Operation(summary = "Verifica se a API esta no ar")
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }
}
