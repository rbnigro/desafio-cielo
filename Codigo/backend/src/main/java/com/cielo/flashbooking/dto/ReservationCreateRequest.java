package com.cielo.flashbooking.dto;

import jakarta.validation.constraints.Positive;

public record ReservationCreateRequest(@Positive int quantity) {
}
