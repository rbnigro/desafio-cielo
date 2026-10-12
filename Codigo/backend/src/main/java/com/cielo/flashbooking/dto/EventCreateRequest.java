package com.cielo.flashbooking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record EventCreateRequest(
        @NotBlank @Size(max = 200) String name,
        @NotNull Instant startsAt,
        @Positive int capacity) {
}
