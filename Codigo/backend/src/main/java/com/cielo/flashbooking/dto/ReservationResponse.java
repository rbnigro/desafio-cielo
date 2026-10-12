package com.cielo.flashbooking.dto;

import com.cielo.flashbooking.model.TicketReservation;

import java.time.Instant;
import java.util.UUID;

public record ReservationResponse(
        UUID id,
        UUID eventId,
        int quantity,
        String status,
        Instant createdAt,
        Instant expiresAt) {
    public static ReservationResponse from(TicketReservation reservation) {
        return new ReservationResponse(reservation.getId(), reservation.getEvent().getId(),
                reservation.getQuantity(), reservation.getStatus().name(),
                reservation.getCreatedAt(), reservation.getExpiresAt());
    }
}
