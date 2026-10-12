package com.cielo.flashbooking.service;

import com.cielo.flashbooking.dto.EventCreateRequest;
import com.cielo.flashbooking.dto.EventResponse;
import com.cielo.flashbooking.model.BookingEvent;
import com.cielo.flashbooking.repository.EventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.UUID;

@Service
public class EventService {
    private final EventRepository eventRepository;
    private final Clock clock;

    public EventService(EventRepository eventRepository, Clock clock) {
        this.eventRepository = eventRepository;
        this.clock = clock;
    }

    @Transactional
    public EventResponse create(EventCreateRequest request) {
        BookingEvent event = new BookingEvent(request.name().trim(), request.startsAt(),
                request.capacity(), clock.instant());
        return EventResponse.from(eventRepository.save(event));
    }

    @Transactional(readOnly = true)
    public EventResponse get(UUID id) {
        return eventRepository.findById(id).map(EventResponse::from)
                .orElseThrow(() -> new EventNotFoundException(id));
    }
}
