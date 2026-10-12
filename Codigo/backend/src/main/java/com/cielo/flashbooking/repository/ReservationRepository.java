package com.cielo.flashbooking.repository;

import com.cielo.flashbooking.model.ReservationStatus;
import com.cielo.flashbooking.model.TicketReservation;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReservationRepository extends JpaRepository<TicketReservation, UUID> {
    Optional<TicketReservation> findByEvent_IdAndIdempotencyKey(UUID eventId, String idempotencyKey);

    List<TicketReservation> findByEvent_IdAndStatusAndExpiresAtLessThanEqual(
            UUID eventId, ReservationStatus status, Instant now);

    @Query("select r.event.id from TicketReservation r where r.id = :id")
    Optional<UUID> findEventIdByReservationId(@Param("id") UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from TicketReservation r where r.id = :id")
    Optional<TicketReservation> findByIdForUpdate(@Param("id") UUID id);

    @Query("select distinct r.event.id from TicketReservation r " +
            "where r.status = :status and r.expiresAt <= :now")
    List<UUID> findEventIdsWithExpiredReservations(
            @Param("status") ReservationStatus status, @Param("now") Instant now, Pageable pageable);
}
