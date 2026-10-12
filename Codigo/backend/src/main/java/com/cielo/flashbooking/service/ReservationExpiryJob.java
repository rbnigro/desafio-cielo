package com.cielo.flashbooking.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ReservationExpiryJob {
    private final ReservationService reservationService;

    public ReservationExpiryJob(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @Scheduled(fixedDelayString = "${booking.expiration.sweep-interval-ms:1000}")
    public void expireDueReservations() {
        for (UUID eventId : reservationService.findEventsWithExpiredReservations()) {
            reservationService.expireDueReservations(eventId);
        }
    }
}
