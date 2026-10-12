package com.cielo.flashbooking.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "reservations", uniqueConstraints =
        @UniqueConstraint(name = "uk_reservation_event_idempotency", columnNames = {"event_id", "idempotency_key"}))
public class TicketReservation {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private BookingEvent event;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private ReservationStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "idempotency_key", nullable = false, length = 128)
    private String idempotencyKey;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "request_hash", nullable = false, length = 64)
    private String requestHash;

    protected TicketReservation() {
    }

    public TicketReservation(BookingEvent event, int quantity, Instant createdAt,
            Instant expiresAt, String idempotencyKey, String requestHash) {
        this.event = event;
        this.quantity = quantity;
        this.status = ReservationStatus.PENDING;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.idempotencyKey = idempotencyKey;
        this.requestHash = requestHash;
    }

    public boolean expireIfDue(Instant now) {
        if (status == ReservationStatus.PENDING && !expiresAt.isAfter(now)) {
            status = ReservationStatus.EXPIRED;
            return true;
        }
        return false;
    }

    public boolean cancel() {
        if (status != ReservationStatus.PENDING) {
            return false;
        }
        status = ReservationStatus.CANCELLED;
        return true;
    }

    public UUID getId() { return id; }
    public BookingEvent getEvent() { return event; }
    public int getQuantity() { return quantity; }
    public ReservationStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getExpiresAt() { return expiresAt; }
    public String getRequestHash() { return requestHash; }
}
