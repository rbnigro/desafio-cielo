package com.cielo.flashbooking.controller;

import com.cielo.flashbooking.dto.EventCreateRequest;
import com.cielo.flashbooking.dto.EventResponse;
import com.cielo.flashbooking.dto.ReservationCreateRequest;
import com.cielo.flashbooking.dto.ReservationResponse;
import com.cielo.flashbooking.service.EventService;
import com.cielo.flashbooking.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/events")
@Tag(name = "Events", description = "Criacao e consulta de eventos e reservas")
public class EventsController {
    private final EventService eventService;
    private final ReservationService reservationService;

    public EventsController(EventService eventService, ReservationService reservationService) {
        this.eventService = eventService;
        this.reservationService = reservationService;
    }

    @PostMapping
    @Operation(summary = "Cria um evento")
    public ResponseEntity<EventResponse> create(@Valid @RequestBody EventCreateRequest request) {
        EventResponse event = eventService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(event.id()).toUri();
        return ResponseEntity.created(location).body(event);
    }

    @GetMapping("/{eventId}")
    @Operation(summary = "Consulta evento e disponibilidade")
    public EventResponse get(@PathVariable UUID eventId) {
        return eventService.get(eventId);
    }

    @PostMapping("/{eventId}/reservations")
    @Operation(summary = "Reserva ingressos para um evento")
    public ResponseEntity<ReservationResponse> reserve(
            @PathVariable UUID eventId,
            @Valid @RequestBody ReservationCreateRequest request,
            @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey) {
        ReservationResponse reservation = reservationService.create(eventId, request, idempotencyKey);
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/reservations/{id}").buildAndExpand(reservation.id()).toUri();
        return ResponseEntity.created(location).body(reservation);
    }
}
