package com.cielo.flashbooking;

import com.cielo.flashbooking.dto.EventCreateRequest;
import com.cielo.flashbooking.dto.ReservationCreateRequest;
import com.cielo.flashbooking.repository.EventRepository;
import com.cielo.flashbooking.repository.ReservationRepository;
import com.cielo.flashbooking.service.CapacityExceededException;
import com.cielo.flashbooking.service.EventService;
import com.cielo.flashbooking.service.ReservationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
class MySqlConcurrencyIntegrationTest {
    @Container
    static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.4")
            .withDatabaseName("flash_booking")
            .withUsername("flash_booking")
            .withPassword("test-password");

    @DynamicPropertySource
    static void mysqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.flyway.enabled", () -> "true");
    }

    @Autowired private EventService eventService;
    @Autowired private ReservationService reservationService;
    @Autowired private EventRepository eventRepository;
    @Autowired private ReservationRepository reservationRepository;

    @BeforeEach
    void clearDatabase() {
        reservationRepository.deleteAll();
        eventRepository.deleteAll();
    }

    @Test
    void mysqlLockPreventsOversellingAcrossConcurrentTransactions() throws Exception {
        var event = eventService.create(new EventCreateRequest(
                "MySQL Flash Sale", Instant.now().plusSeconds(3600), 5));
        int requestCount = 20;
        var ready = new CountDownLatch(requestCount);
        var start = new CountDownLatch(1);
        var executor = Executors.newFixedThreadPool(requestCount);
        try {
            List<java.util.concurrent.Future<Boolean>> results = new ArrayList<>();
            for (int i = 0; i < requestCount; i++) {
                int requestId = i;
                results.add(executor.submit(() -> {
                    ready.countDown();
                    if (!start.await(10, TimeUnit.SECONDS)) {
                        throw new IllegalStateException("Concurrency test did not start");
                    }
                    try {
                        reservationService.create(event.id(), new ReservationCreateRequest(1),
                                "mysql-parallel-" + requestId);
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
                if (result.get(45, TimeUnit.SECONDS)) {
                    accepted++;
                }
            }
            assertThat(accepted).isEqualTo(5);
            assertThat(reservationRepository.count()).isEqualTo(5);
            assertThat(eventService.get(event.id()).availableTickets()).isZero();
        } finally {
            executor.shutdownNow();
        }
    }
}
