package com.cielo.flashbooking.controller;

import com.cielo.flashbooking.dto.ReservationResponse;
import com.cielo.flashbooking.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/reservations")
@Tag(name = "Reservations", description = "Consulta e cancelamento de reservas")
public class ReservationsController {
    private final ReservationService reservationService;

    public ReservationsController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping("/{reservationId}")
    @Operation(summary = "Consulta uma reserva")
    public ReservationResponse get(@PathVariable UUID reservationId) {
        return reservationService.get(reservationId);
    }

    @DeleteMapping("/{reservationId}")
    @Operation(summary = "Cancela uma reserva pendente")
    public ResponseEntity<Void> cancel(@PathVariable UUID reservationId) {
        reservationService.cancel(reservationId);
        return ResponseEntity.noContent().build();
    }
}
