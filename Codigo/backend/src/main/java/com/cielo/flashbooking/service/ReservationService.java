package com.cielo.flashbooking.service;

import com.cielo.flashbooking.dto.ReservationCreateRequest;
import com.cielo.flashbooking.dto.ReservationResponse;
import com.cielo.flashbooking.model.BookingEvent;
import com.cielo.flashbooking.model.ReservationStatus;
import com.cielo.flashbooking.model.TicketReservation;
import com.cielo.flashbooking.repository.EventRepository;
import com.cielo.flashbooking.repository.ReservationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Service
public class ReservationService {
    private static final int EXPIRATION_BATCH_SIZE = 100;

    private final EventRepository eventRepository;
    private final ReservationRepository reservationRepository;
    private final Clock clock;
    private final Duration reservationTtl;

    public ReservationService(EventRepository eventRepository,
            ReservationRepository reservationRepository, Clock clock,
            @Value("${booking.reservation.ttl:PT15M}") Duration reservationTtl) {
        if (reservationTtl.isNegative() || reservationTtl.isZero()) {
            throw new IllegalArgumentException("Reservation TTL must be greater than zero");
        }
        this.eventRepository = eventRepository;
        this.reservationRepository = reservationRepository;
        this.clock = clock;
        this.reservationTtl = reservationTtl;
    }

    @Transactional
    public ReservationResponse create(UUID eventId, ReservationCreateRequest request, String idempotencyKey) {
        validateIdempotencyKey(idempotencyKey);
        if (request.quantity() <= 0) {
            throw new InvalidRequestException("quantity must be greater than zero");
        }

        BookingEvent event = lockEvent(eventId);
        Instant now = clock.instant();
        expireDueReservations(event, now);

        String requestHash = hashRequest(request);
        var existing = reservationRepository.findByEvent_IdAndIdempotencyKey(eventId, idempotencyKey);
        if (existing.isPresent()) {
            TicketReservation prior = existing.get();
            if (!MessageDigest.isEqual(
                    prior.getRequestHash().getBytes(StandardCharsets.US_ASCII),
                    requestHash.getBytes(StandardCharsets.US_ASCII))) {
                throw new IdempotencyConflictException();
            }
            return ReservationResponse.from(prior);
        }

        if (request.quantity() > event.getAvailableTickets()) {
            throw new CapacityExceededException();
        }

        event.reserve(request.quantity());
        TicketReservation reservation = new TicketReservation(event, request.quantity(), now,
                now.plus(reservationTtl), idempotencyKey, requestHash);
        return ReservationResponse.from(reservationRepository.save(reservation));
    }

    @Transactional
    public ReservationResponse get(UUID reservationId) {
        UUID eventId = findEventId(reservationId);
        BookingEvent event = lockEvent(eventId);
        expireDueReservations(event, clock.instant());
        return reservationRepository.findById(reservationId).map(ReservationResponse::from)
                .orElseThrow(() -> new ReservationNotFoundException(reservationId));
    }

    @Transactional
    public void cancel(UUID reservationId) {
        UUID eventId = findEventId(reservationId);
        BookingEvent event = lockEvent(eventId);
        expireDueReservations(event, clock.instant());
        TicketReservation reservation = reservationRepository.findByIdForUpdate(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException(reservationId));
        if (reservation.cancel()) {
            event.release(reservation.getQuantity());
        }
    }

    @Transactional
    public void expireDueReservations(UUID eventId) {
        BookingEvent event = lockEvent(eventId);
        expireDueReservations(event, clock.instant());
    }

    @Transactional(readOnly = true)
    public List<UUID> findEventsWithExpiredReservations() {
        return reservationRepository.findEventIdsWithExpiredReservations(
                ReservationStatus.PENDING, clock.instant(), PageRequest.of(0, EXPIRATION_BATCH_SIZE));
    }

    private UUID findEventId(UUID reservationId) {
        return reservationRepository.findEventIdByReservationId(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException(reservationId));
    }

    private BookingEvent lockEvent(UUID eventId) {
        return eventRepository.findByIdForUpdate(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));
    }

    private void expireDueReservations(BookingEvent event, Instant now) {
        List<TicketReservation> expired = reservationRepository
                .findByEvent_IdAndStatusAndExpiresAtLessThanEqual(
                        event.getId(), ReservationStatus.PENDING, now);
        int releasedTickets = 0;
        for (TicketReservation reservation : expired) {
            if (reservation.expireIfDue(now)) {
                releasedTickets += reservation.getQuantity();
            }
        }
        if (releasedTickets > 0) {
            event.release(releasedTickets);
        }
    }

    private void validateIdempotencyKey(String key) {
        if (key == null || key.isBlank() || key.length() > 128) {
            throw new InvalidRequestException("Idempotency-Key must contain 1 to 128 characters");
        }
    }

    private String hashRequest(ReservationCreateRequest request) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(Integer.toString(request.quantity()).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }
}
