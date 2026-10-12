package com.cielo.flashbooking;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.cielo.flashbooking.dto.EventCreateRequest;
import com.cielo.flashbooking.dto.EventResponse;
import com.cielo.flashbooking.dto.ReservationCreateRequest;
import com.cielo.flashbooking.model.BookingEvent;
import com.cielo.flashbooking.model.ReservationStatus;
import com.cielo.flashbooking.repository.EventRepository;
import com.cielo.flashbooking.repository.ReservationRepository;
import com.cielo.flashbooking.service.CapacityExceededException;
import com.cielo.flashbooking.service.EventService;
import com.cielo.flashbooking.service.ReservationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(FlashBookingIntegrationTest.ClockTestConfiguration.class)
class FlashBookingIntegrationTest {
    private static final Instant INITIAL_TIME = Instant.parse("2026-10-08T16:00:00Z");

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private EventService eventService;
    @Autowired private ReservationService reservationService;
    @Autowired private EventRepository eventRepository;
    @Autowired private ReservationRepository reservationRepository;
    @Autowired private MutableClock clock;

    @BeforeEach
    void clearDatabase() {
        reservationRepository.deleteAll();
        eventRepository.deleteAll();
        clock.setInstant(INITIAL_TIME);
    }

    @Test
    void healthEndpointReportsApplicationIsUp() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void openApiSpecificationDocumentsHealthAndReservationRoutes() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("Flash Booking API"))
                .andExpect(jsonPath("$.paths['/health'].get").exists())
                .andExpect(jsonPath("$.paths['/events/{eventId}/reservations'].post").exists())
                .andExpect(jsonPath("$.paths['/reservations/{reservationId}'].delete").exists());
    }

    @Test
    void createsEventAndReturnsAvailability() throws Exception {
        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Live Demo","startsAt":"2027-06-20T18:00:00Z","capacity":4}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.availableTickets").value(4))
                .andExpect(jsonPath("$.capacity").value(4));
    }

    @Test
    void rejectsInvalidEventAndMissingIdempotencyKeyWithStructuredErrors() throws Exception {
        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Invalid","startsAt":"2027-06-20T18:00:00Z","capacity":0}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));

        EventResponse event = createEvent(2);
        mockMvc.perform(post("/events/{id}/reservations", event.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    void returnsStructuredNotFoundErrors() throws Exception {
        UUID missingId = UUID.randomUUID();
        mockMvc.perform(get("/events/{id}", missingId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
        mockMvc.perform(get("/reservations/{id}", missingId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void reservationIsIdempotentAndRejectsChangedPayloadForSameKey() throws Exception {
        EventResponse event = createEvent(4);
        String first = mockMvc.perform(post("/events/{id}/reservations", event.id())
                        .header("Idempotency-Key", "same-request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":2}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String reservationId = objectMapper.readTree(first).get("id").asText();

        mockMvc.perform(post("/events/{id}/reservations", event.id())
                        .header("Idempotency-Key", "same-request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":2}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(reservationId));

        mockMvc.perform(post("/events/{id}/reservations", event.id())
                        .header("Idempotency-Key", "same-request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":1}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("IDEMPOTENCY_KEY_REUSED"));
    }

    @Test
    void cancellationRestoresCapacityOnlyOnce() throws Exception {
        EventResponse event = createEvent(3);
        String response = mockMvc.perform(post("/events/{id}/reservations", event.id())
                        .header("Idempotency-Key", "cancel-me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":2}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String reservationId = objectMapper.readTree(response).get("id").asText();

        mockMvc.perform(delete("/reservations/{id}", reservationId))
                .andExpect(status().isNoContent());
        mockMvc.perform(delete("/reservations/{id}", reservationId))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/events/{id}", event.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableTickets").value(3));
        mockMvc.perform(get("/reservations/{id}", reservationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void expirationRestoresCapacityAndUpdatesReservationStatus() throws Exception {
        EventResponse event = createEvent(2);
        var reservation = reservationService.create(event.id(),
                new ReservationCreateRequest(2), "expires");
        clock.advance(Duration.ofMinutes(16));

        mockMvc.perform(get("/reservations/{id}", reservation.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EXPIRED"));
        mockMvc.perform(get("/events/{id}", event.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableTickets").value(2));
        mockMvc.perform(get("/reservations/{id}", reservation.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EXPIRED"));
        mockMvc.perform(get("/events/{id}", event.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableTickets").value(2));
    }

    @Test
    void doesNotOversellUnderConcurrentReservations() throws Exception {
        EventResponse event = createEvent(8);
        int requests = 32;
        var ready = new CountDownLatch(requests);
        var start = new CountDownLatch(1);
        var executor = Executors.newFixedThreadPool(requests);
        try {
            List<java.util.concurrent.Future<Boolean>> results = new ArrayList<>();
            for (int i = 0; i < requests; i++) {
                int requestNumber = i;
                results.add(executor.submit(() -> {
                    ready.countDown();
                    if (!start.await(10, TimeUnit.SECONDS)) {
                        throw new IllegalStateException("Concurrency test did not start");
                    }
                    try {
                        reservationService.create(event.id(), new ReservationCreateRequest(1),
                                "parallel-" + requestNumber);
                        return true;
                    } catch (CapacityExceededException expected) {
                        return false;
                    }
                }));
            }
            assertThat(ready.await(10, TimeUnit.SECONDS)).isTrue();
            start.countDown();
            long accepted = 0;
            for (var result : results) {
                if (result.get(30, TimeUnit.SECONDS)) {
                    accepted++;
                }
            }

            assertThat(accepted).isEqualTo(8);
            assertThat(reservationRepository.count()).isEqualTo(8);
            assertThat(eventService.get(event.id()).availableTickets()).isZero();
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void returnsConflictWhenCapacityIsExhausted() throws Exception {
        EventResponse event = createEvent(1);
        reservationService.create(event.id(), new ReservationCreateRequest(1), "first");

        mockMvc.perform(post("/events/{id}/reservations", event.id())
                        .header("Idempotency-Key", "second")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":1}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INSUFFICIENT_CAPACITY"));
    }

    private EventResponse createEvent(int capacity) {
        return eventService.create(new EventCreateRequest(
                "Test Event", INITIAL_TIME.plus(Duration.ofDays(1)), capacity));
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class ClockTestConfiguration {
        @Bean
        @Primary
        MutableClock mutableClock() {
            return new MutableClock(INITIAL_TIME);
        }
    }

    static class MutableClock extends Clock {
        private Instant instant;

        MutableClock(Instant instant) {
            this.instant = instant;
        }

        synchronized void setInstant(Instant instant) {
            this.instant = instant;
        }

        synchronized void advance(Duration duration) {
            instant = instant.plus(duration);
        }

        @Override
        public synchronized Instant instant() {
            return instant;
        }

        @Override
        public ZoneId getZone() {
            return ZoneId.of("UTC");
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }
    }
}
