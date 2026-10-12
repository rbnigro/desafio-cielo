package com.cielo.flashbooking.dto;

import com.cielo.flashbooking.model.BookingEvent;

import java.time.Instant;
import java.util.UUID;

public record EventResponse(UUID id, String name, Instant startsAt, int capacity, int availableTickets) {
    public static EventResponse from(BookingEvent event) {
        return new EventResponse(event.getId(), event.getName(), event.getStartsAt(),
                event.getCapacity(), event.getAvailableTickets());
    }
}
