package com.cielo.flashbooking.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "events")
public class BookingEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private UUID id;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "starts_at", nullable = false)
    private Instant startsAt;

    @Column(name = "capacity", nullable = false)
    private int capacity;

    @Column(name = "available_tickets", nullable = false)
    private int availableTickets;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected BookingEvent() {
    }

    public BookingEvent(String name, Instant startsAt, int capacity, Instant createdAt) {
        this.name = name;
        this.startsAt = startsAt;
        this.capacity = capacity;
        this.availableTickets = capacity;
        this.createdAt = createdAt;
    }

    public void reserve(int quantity) {
        if (quantity <= 0 || quantity > availableTickets) {
            throw new IllegalArgumentException("Reservation quantity exceeds available inventory");
        }
        availableTickets -= quantity;
    }

    public void release(int quantity) {
        if (quantity < 0 || availableTickets + quantity > capacity) {
            throw new IllegalStateException("Released inventory would exceed event capacity");
        }
        availableTickets += quantity;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public Instant getStartsAt() { return startsAt; }
    public int getCapacity() { return capacity; }
    public int getAvailableTickets() { return availableTickets; }
    public Instant getCreatedAt() { return createdAt; }
}
